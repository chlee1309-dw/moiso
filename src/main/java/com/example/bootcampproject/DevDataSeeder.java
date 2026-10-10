package com.example.bootcampproject;

import com.example.bootcampproject.entity.*;
import com.example.bootcampproject.entity.enums.*;
import com.example.bootcampproject.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 로컬 수동 테스트용 더미 데이터 시더.
 * application.yaml의 app.seed.enabled=true 일 때만 동작하고, users 테이블에 이미 데이터가 있으면
 * 아무것도 하지 않는다(중복 시딩 방지 - 서버 재시작해도 안전).
 * 생성되는 모든 테스트 계정 비밀번호는 "test1234!".
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DevDataSeeder implements CommandLineRunner {

    private static final String TEST_PASSWORD = "test1234!";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionLikeRepository questionLikeRepository;
    private final AnswerLikeRepository answerLikeRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final FriendRepository friendRepository;
    private final NotificationRepository notificationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final SearchKeywordRepository searchKeywordRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail("minjun@test.com")) {
            log.info("[DevDataSeeder] 이미 시드 데이터가 있어 시딩을 건너뜁니다.");
            return;
        }

        List<UserEntity> users = seedUsers();
        List<LocationEntity> locations = seedLocations();
        List<QuestionEntity> questions = seedQuestions(users);
        List<AnswerEntity> answers = seedAnswers(users, questions);
        seedLikes(users, questions, answers);
        List<AppointmentEntity> appointments = seedAppointments(users, locations);
        seedReviews(appointments);
        seedFriends(users);
        seedNotifications(users, questions, appointments);
        seedChat(users);
        seedCalendarEvents(users, appointments);
        seedSearchKeywords();

        log.info("[DevDataSeeder] 테스트 데이터 시딩 완료. 로그인 비밀번호는 전부 '{}' 입니다.", TEST_PASSWORD);
    }

    // ===== 사용자 =====

    private List<UserEntity> seedUsers() {
        String pw = passwordEncoder.encode(TEST_PASSWORD);
        List<UserEntity> users = List.of(
                user("김민준", "minjun@test.com", pw, Purpose.SCHOOL, "한양대학교", "컴퓨터공학과"),
                user("이서연", "seoyeon@test.com", pw, Purpose.CAREER, "연세대학교", "경영학과"),
                user("박지훈", "jihoon@test.com", pw, Purpose.COUNSEL, "고려대학교", "심리학과"),
                user("최유진", "yujin@test.com", pw, Purpose.SCHOOL, "서울대학교", "전기정보공학부"),
                user("정도윤", "doyoon@test.com", pw, Purpose.CAREER, "성균관대학교", "소프트웨어학과"),
                user("강하은", "haeun@test.com", pw, Purpose.COUNSEL, "이화여자대학교", "국어국문학과"),
                user("윤서준", "seojun@test.com", pw, Purpose.SCHOOL, "한국외국어대학교", "영어통번역학과"),
                user("임채원", "chaewon@test.com", pw, Purpose.CAREER, "중앙대학교", "경제학과")
        );
        return userRepository.saveAll(users);
    }

    private UserEntity user(String name, String email, String encodedPw, Purpose purpose, String univ, String dept) {
        return UserEntity.builder()
                .name(name).email(email).password(encodedPw)
                .purpose(purpose).isVerified(true)
                .university(univ).department(dept)
                .build();
    }

    // ===== 장소 =====

    private List<LocationEntity> seedLocations() {
        List<LocationEntity> locations = List.of(
                LocationEntity.builder().name("중앙도서관 스터디룸").category("스터디룸")
                        .description("24시간 이용 가능한 조용한 스터디룸").available(true).build(),
                LocationEntity.builder().name("학생회관 카페").category("카페")
                        .description("편하게 대화하며 멘토링하기 좋은 카페").available(true).build(),
                LocationEntity.builder().name("공학관 세미나실 201호").category("세미나실")
                        .description("프로젝터가 있는 소규모 세미나실").available(true).build(),
                LocationEntity.builder().name("온라인 (Zoom)").category("온라인")
                        .description("화상으로 진행하는 온라인 멘토링").available(true).build(),
                LocationEntity.builder().name("후생관 휴게공간").category("휴게공간")
                        .description("리모델링 중 - 임시 이용 불가").available(false).build()
        );
        return locationRepository.saveAll(locations);
    }

    // ===== 질문 =====

    private List<QuestionEntity> seedQuestions(List<UserEntity> u) {
        List<QuestionEntity> qs = List.of(
                question(QuestionCategory.TODAY, "오늘 도서관 자리 진짜 없네요 다들 어디서 공부하세요?",
                        "시험기간도 아닌데 중앙도서관이 꽉 찼어요. 다들 자주 가는 공부 장소 있으면 추천 부탁드려요!", u.get(0)),
                question(QuestionCategory.FREE, "신입생 환영회 다들 가시나요?",
                        "이번 주 금요일 학과 신입생 환영회 있던데 가는 사람 있나요? 혼자 가긴 좀 어색해서요.", u.get(1)),
                question(QuestionCategory.WORRY_CAREER, "휴학하고 인턴 할지 그냥 졸업할지 고민이에요",
                        "졸업까지 한 학기 남았는데 좋은 인턴 자리가 나서 휴학을 할지 고민입니다. 선배님들 조언 부탁드려요.", u.get(2)),
                question(QuestionCategory.WORRY_STUDY, "전공 수업 따라가기 너무 힘든데 다들 어떻게 극복하셨나요",
                        "2학년 전공 수업부터 갑자기 어려워졌어요. 공부 방법 팁 있으면 알려주세요.", u.get(3)),
                question(QuestionCategory.WORRY_RELATION, "조모임 하다가 트러블 생겼는데 어떻게 풀어야 할까요",
                        "팀플하다가 역할 분담 문제로 조원이랑 서먹해졌어요. 먼저 연락해서 풀어야 할까요?", u.get(5)),
                question(QuestionCategory.MENTOR_FIND, "백엔드 개발 멘토 구합니다 (스프링 하시는 분)",
                        "스프링부트로 토이 프로젝트 진행 중인데, 코드 리뷰 해주실 멘토님 구합니다!", u.get(4)),
                question(QuestionCategory.MENTOR_REVIEW, "지난주 취업 멘토링 후기 남깁니다",
                        "취업 준비하면서 막막했는데 멘토님 덕분에 방향을 잡았어요. 다들 멘토링 꼭 받아보세요.", u.get(6)),
                question(QuestionCategory.STUDY, "알고리즘 스터디원 모집합니다 (주 2회)",
                        "코딩테스트 대비 알고리즘 스터디 하실 분 모집합니다. 화/목 저녁 온라인으로 진행 예정이에요.", u.get(7)),
                question(QuestionCategory.MARKET, "전공서적 팝니다 (자료구조, 운영체제)",
                        "작년에 산 전공서적 두 권 팝니다. 필기 거의 없이 깨끗해요. 학교 앞에서 직거래 가능합니다.", u.get(0)),
                question(QuestionCategory.WORRY_CAREER, "대학원 진학 vs 취업, 선배님들 조언 부탁드려요",
                        "교수님은 대학원을 추천하시는데 저는 빨리 취업하고 싶어요. 어떤 게 나을까요?", u.get(1))
        );
        return questionRepository.saveAll(qs);
    }

    private QuestionEntity question(QuestionCategory category, String title, String content, UserEntity writer) {
        QuestionEntity q = QuestionEntity.builder().category(category).title(title).content(content).build();
        q.setWriter(writer);
        return q;
    }

    // ===== 답변 / 대댓글 =====

    private List<AnswerEntity> seedAnswers(List<UserEntity> u, List<QuestionEntity> q) {
        List<AnswerEntity> topLevel = List.of(
                answer("도서관 대신 중앙도서관 옆 세미나동 4층 가보세요! 사람 적고 콘센트도 많아요.", q.get(0), u.get(1)),
                answer("저도 갈 예정이에요! 같이 가실 분 구해요 ㅎㅎ", q.get(0), u.get(5)),
                answer("인턴 자리가 흔치 않은 기회라면 휴학 추천드려요. 저도 같은 고민했었는데 인턴 경험이 취업에 훨씬 도움됐어요.", q.get(2), u.get(4)),
                answer("전공 스터디 만들어서 매주 문제 풀어보는 걸 추천해요. 혼자 하면 금방 지쳐요.", q.get(3), u.get(7)),
                answer("먼저 연락해서 오해를 푸는 게 나을 것 같아요. 시간 끌면 더 어색해져요.", q.get(4), u.get(1)),
                answer("스프링 부트 3년차입니다. 편하게 DM 주세요! 코드 리뷰 가능해요.", q.get(5), u.get(6)),
                answer("저도 관심있어요! 참여하고 싶습니다.", q.get(7), u.get(0)),
                answer("책 상태 괜찮으면 자료구조 책 구매하고 싶어요. 쪽지 드릴게요.", q.get(8), u.get(3)),
                answer("취업 먼저 하고 필요하면 대학원은 나중에 가도 늦지 않아요.", q.get(9), u.get(2))
        );
        List<AnswerEntity> savedTop = answerRepository.saveAll(topLevel);

        List<AnswerEntity> replies = List.of(
                reply("오 감사합니다! 오늘 바로 가볼게요.", savedTop.get(0), u.get(0)),
                reply("조언 감사해요. 인턴 먼저 알아보고 결정할게요!", savedTop.get(2), u.get(2)),
                reply("오 감사합니다! DM 드릴게요.", savedTop.get(5), u.get(4))
        );
        List<AnswerEntity> savedReplies = answerRepository.saveAll(replies);

        List<AnswerEntity> all = new ArrayList<>(savedTop);
        all.addAll(savedReplies);
        return all;
    }

    private AnswerEntity answer(String content, QuestionEntity question, UserEntity writer) {
        AnswerEntity a = AnswerEntity.builder().content(content).build();
        a.setQuestion(question);
        a.setWriter(writer);
        return a;
    }

    private AnswerEntity reply(String content, AnswerEntity parent, UserEntity writer) {
        AnswerEntity a = AnswerEntity.builder().content(content).build();
        a.setQuestion(parent.getQuestion());
        a.setWriter(writer);
        a.setParent(parent);
        return a;
    }

    // ===== 좋아요 (오늘의 베스트 고민 / 이달의 멘토 랭킹용) =====

    private void seedLikes(List<UserEntity> u, List<QuestionEntity> q, List<AnswerEntity> a) {
        List<QuestionLikeEntity> qLikes = new ArrayList<>();
        addQuestionLikes(qLikes, q.get(0), u.get(1), u.get(2), u.get(3), u.get(4), u.get(5));
        addQuestionLikes(qLikes, q.get(2), u.get(0), u.get(1), u.get(3), u.get(6));
        addQuestionLikes(qLikes, q.get(5), u.get(0), u.get(1), u.get(2), u.get(3));
        addQuestionLikes(qLikes, q.get(6), u.get(0), u.get(2));
        addQuestionLikes(qLikes, q.get(7), u.get(1), u.get(4));
        addQuestionLikes(qLikes, q.get(1), u.get(3));
        addQuestionLikes(qLikes, q.get(3), u.get(5));
        addQuestionLikes(qLikes, q.get(8), u.get(4));
        addQuestionLikes(qLikes, q.get(9), u.get(2));
        addQuestionLikes(qLikes, q.get(4), u.get(7));
        questionLikeRepository.saveAll(qLikes);

        // 답변 idx: 0~8 = 최상위 답변, 9~11 = 대댓글(9→ans0, 10→ans2, 11→ans5)
        List<AnswerLikeEntity> aLikes = new ArrayList<>();
        addAnswerLikes(aLikes, a.get(0), u.get(0), u.get(2), u.get(3));
        addAnswerLikes(aLikes, a.get(2), u.get(0), u.get(1), u.get(2), u.get(3));
        addAnswerLikes(aLikes, a.get(5), u.get(0), u.get(2), u.get(4), u.get(5));
        addAnswerLikes(aLikes, a.get(3), u.get(0), u.get(1));
        addAnswerLikes(aLikes, a.get(8), u.get(5));
        addAnswerLikes(aLikes, a.get(11), u.get(5));
        answerLikeRepository.saveAll(aLikes);
    }

    private void addQuestionLikes(List<QuestionLikeEntity> list, QuestionEntity question, UserEntity... likers) {
        for (UserEntity liker : likers) {
            QuestionLikeEntity like = new QuestionLikeEntity();
            like.setUser(liker);
            like.setQuestion(question);
            list.add(like);
        }
    }

    private void addAnswerLikes(List<AnswerLikeEntity> list, AnswerEntity answer, UserEntity... likers) {
        for (UserEntity liker : likers) {
            AnswerLikeEntity like = new AnswerLikeEntity();
            like.setUser(liker);
            like.setAnswer(answer);
            list.add(like);
        }
    }

    // ===== 예약 =====

    private List<AppointmentEntity> seedAppointments(List<UserEntity> u, List<LocationEntity> loc) {
        LocalDate today = LocalDate.now();
        List<AppointmentEntity> appts = List.of(
                appointment(today.plusDays(1), LocalTime.of(14, 0), AppointmentStatus.REQUESTED, u.get(2), u.get(4), loc.get(0)),
                appointment(today.plusDays(3), LocalTime.of(16, 30), AppointmentStatus.ACCEPTED, u.get(3), u.get(6), loc.get(1)),
                appointment(today.plusDays(5), LocalTime.of(11, 0), AppointmentStatus.REQUESTED, u.get(5), u.get(1), loc.get(3)),
                appointment(today.minusDays(2), LocalTime.of(15, 0), AppointmentStatus.DONE, u.get(7), u.get(4), loc.get(2)),
                appointment(today.minusDays(7), LocalTime.of(13, 0), AppointmentStatus.DONE, u.get(0), u.get(6), loc.get(1)),
                appointment(today.plusDays(1), LocalTime.of(10, 0), AppointmentStatus.CANCELLED, u.get(2), u.get(3), loc.get(3))
        );
        return appointmentRepository.saveAll(appts);
    }

    private AppointmentEntity appointment(LocalDate date, LocalTime time, AppointmentStatus status,
                                          UserEntity mentee, UserEntity mentor, LocationEntity location) {
        AppointmentEntity a = AppointmentEntity.builder().date(date).time(time).status(status).build();
        a.setMentee(mentee);
        a.setMentor(mentor);
        a.setLocation(location);
        return a;
    }

    // ===== 리뷰 (완료된 예약 2건) =====

    private void seedReviews(List<AppointmentEntity> appts) {
        AppointmentEntity a1 = appts.get(3);
        AppointmentEntity a2 = appts.get(4);

        ReviewEntity r1 = ReviewEntity.builder()
                .rating(new BigDecimal("4.5"))
                .content("세미나실이 조용하고 좋았어요. 멘토님이 진로 방향 잡는 데 큰 도움 주셨습니다.")
                .build();
        r1.setAppointment(a1);
        r1.setMentee(a1.getMentee());
        r1.setMentor(a1.getMentor());

        ReviewEntity r2 = ReviewEntity.builder()
                .rating(new BigDecimal("5.0"))
                .content("취업 준비 관련해서 정말 현실적인 조언 많이 얻었습니다. 감사합니다!")
                .build();
        r2.setAppointment(a2);
        r2.setMentee(a2.getMentee());
        r2.setMentor(a2.getMentor());

        reviewRepository.saveAll(List.of(r1, r2));
    }

    // ===== 친구 =====

    private void seedFriends(List<UserEntity> u) {
        List<FriendEntity> friends = List.of(
                friend(u.get(0), u.get(1), FriendStatus.ACCEPTED),
                friend(u.get(2), u.get(3), FriendStatus.ACCEPTED),
                friend(u.get(4), u.get(5), FriendStatus.REQUESTED),
                friend(u.get(6), u.get(7), FriendStatus.ACCEPTED),
                friend(u.get(1), u.get(2), FriendStatus.REQUESTED)
        );
        friendRepository.saveAll(friends);
    }

    private FriendEntity friend(UserEntity requester, UserEntity addressee, FriendStatus status) {
        FriendEntity f = new FriendEntity();
        f.setRequester(requester);
        f.setAddressee(addressee);
        f.setStatus(status);
        return f;
    }

    // ===== 알림 =====

    private void seedNotifications(List<UserEntity> u, List<QuestionEntity> q, List<AppointmentEntity> appts) {
        List<NotificationEntity> notifs = List.of(
                notification(u.get(2), NotificationType.ANSWER, "'휴학하고 인턴 할지...' 질문에 답변이 달렸습니다.",
                        "/questions/" + q.get(2).getIdx(), false),
                notification(u.get(0), NotificationType.REPLY, "회원님의 답변에 대댓글이 달렸습니다.",
                        "/questions/" + q.get(0).getIdx(), false),
                notification(u.get(0), NotificationType.LIKE_QUESTION, "작성하신 질문에 좋아요가 눌렸습니다.",
                        "/questions/" + q.get(0).getIdx(), true),
                notification(u.get(4), NotificationType.APPOINTMENT_REQUESTED, "박지훈님이 멘토링을 신청했습니다.",
                        "/appointments", false),
                notification(u.get(3), NotificationType.APPOINTMENT_STATUS_CHANGED, "예약이 수락되었습니다.",
                        "/appointments", false),
                notification(u.get(5), NotificationType.FRIEND_REQUEST, "정도윤님이 친구 요청을 보냈습니다.",
                        "/friends", false),
                notification(u.get(0), NotificationType.FRIEND_ACCEPTED, "이서연님과 친구가 되었습니다.",
                        "/friends", true),
                notification(u.get(6), NotificationType.LIKE_ANSWER, "작성하신 답변에 좋아요가 눌렸습니다.",
                        "/questions/" + q.get(5).getIdx(), false)
        );
        notificationRepository.saveAll(notifs);
    }

    private NotificationEntity notification(UserEntity recipient, NotificationType type, String content, String link, boolean read) {
        NotificationEntity n = NotificationEntity.builder().type(type).content(content).link(link).build();
        n.setRecipient(recipient);
        n.setRead(read);
        return n;
    }

    // ===== 채팅 =====

    private void seedChat(List<UserEntity> u) {
        ChatRoomEntity room1 = chatRoom(u.get(2), u.get(4)); // 박지훈 - 정도윤
        ChatRoomEntity room2 = chatRoom(u.get(3), u.get(6)); // 최유진 - 윤서준
        List<ChatRoomEntity> rooms = chatRoomRepository.saveAll(List.of(room1, room2));

        List<ChatMessageEntity> messages = List.of(
                message(rooms.get(0), u.get(2), "안녕하세요! 멘토링 신청 드린 박지훈입니다."),
                message(rooms.get(0), u.get(4), "네 안녕하세요! 편하신 시간대 알려주시면 일정 맞춰볼게요."),
                message(rooms.get(0), u.get(2), "내일 오후 2시쯤 괜찮으실까요?"),
                message(rooms.get(0), u.get(4), "좋습니다, 그때 뵙겠습니다!"),
                message(rooms.get(1), u.get(3), "안녕하세요 서준님, 지난번 질문 답변 감사했습니다."),
                message(rooms.get(1), u.get(6), "별말씀을요! 궁금한 거 있으면 언제든 채팅 주세요."),
                message(rooms.get(1), u.get(3), "이번 주말에 잠깐 카페에서 뵐 수 있을까요?"),
                message(rooms.get(1), u.get(6), "네 좋아요, 토요일 오후 어떠세요?")
        );
        chatMessageRepository.saveAll(messages);
    }

    private ChatRoomEntity chatRoom(UserEntity a, UserEntity b) {
        ChatRoomEntity room = new ChatRoomEntity();
        // 컨벤션: idx가 작은 쪽이 항상 userA (방 중복 생성 방지)
        if (a.getIdx() < b.getIdx()) {
            room.setUserA(a);
            room.setUserB(b);
        } else {
            room.setUserA(b);
            room.setUserB(a);
        }
        return room;
    }

    private ChatMessageEntity message(ChatRoomEntity room, UserEntity sender, String content) {
        ChatMessageEntity m = ChatMessageEntity.builder().content(content).build();
        m.setRoom(room);
        m.setSender(sender);
        return m;
    }

    // ===== 캘린더 =====

    private void seedCalendarEvents(List<UserEntity> u, List<AppointmentEntity> appts) {
        List<CalendarEventEntity> events = List.of(
                calendarEvent(u.get(2), appts.get(0).getDate(), appts.get(0).getTime(), "정도윤 멘토님과 멘토링"),
                calendarEvent(u.get(2), LocalDate.now().plusDays(4), LocalTime.of(9, 0), "중간고사 시작"),
                calendarEvent(u.get(3), appts.get(1).getDate(), appts.get(1).getTime(), "윤서준 멘토님과 멘토링"),
                calendarEvent(u.get(4), LocalDate.now().plusDays(2), LocalTime.of(19, 0), "스터디 모임")
        );
        calendarEventRepository.saveAll(events);
    }

    private CalendarEventEntity calendarEvent(UserEntity user, LocalDate date, LocalTime time, String title) {
        CalendarEventEntity e = CalendarEventEntity.builder().date(date).time(time).title(title).build();
        e.setUser(user);
        return e;
    }

    // ===== 인기 검색어 =====

    private void seedSearchKeywords() {
        seedKeyword("스터디", 8);
        seedKeyword("장학금", 5);
        seedKeyword("인턴", 6);
        seedKeyword("자취", 3);
        seedKeyword("동아리", 4);
    }

    private void seedKeyword(String keyword, int count) {
        SearchKeywordEntity k = new SearchKeywordEntity(keyword);
        for (int i = 1; i < count; i++) {
            k.increment();
        }
        searchKeywordRepository.save(k);
    }
}
