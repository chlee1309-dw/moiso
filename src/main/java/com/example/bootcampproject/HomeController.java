package com.example.bootcampproject;

import com.example.bootcampproject.config.NoLoginRequired;
import com.example.bootcampproject.controller.SessionConst;
import com.example.bootcampproject.dto.response.BestMentorResponse;
import com.example.bootcampproject.dto.response.BestQuestionResponse;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.service.LikeService;
import com.example.bootcampproject.service.NotificationService;
import com.example.bootcampproject.service.QuestionService;
import com.example.bootcampproject.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.List;

/** 프론트 화면(정적 템플릿) 라우팅 전용 컨트롤러. */
@Controller
@RequiredArgsConstructor
public class HomeController {

    /** 메인 "오늘의 베스트 고민" 카드 개수 (부족하면 빈 칸으로 채움). */
    private static final int BEST_QUESTION_SLOTS = 5;

    /** 메인 "이달의 멘토" 카드 개수 (부족하면 빈 칸으로 채움). */
    private static final int BEST_MENTOR_SLOTS = 5;

    /** 메인 "내가 작성한 게시물" / "최근 게시물" 목록에 보여줄 최대 개수. */
    private static final int MY_POST_SLOTS = 5;
    private static final int RECENT_POST_SLOTS = 5;

    private final LikeService likeService;
    private final NotificationService notificationService;
    private final SearchKeywordService searchKeywordService;
    private final QuestionService questionService;

    /** 게시글 상세 (임시로 베스트글 화면 재사용). */
    @NoLoginRequired
    @GetMapping("/post")
    public String post() {
        return "Best_Post";
    }

    @NoLoginRequired
    @GetMapping({"/", "/index"})
    public String index() {
        return "Index";
    }

    @NoLoginRequired
    @GetMapping("/login")
    public String login() {
        return "Login";
    }

    @NoLoginRequired
    @GetMapping("/signup")
    public String signup() {
        return "Sing_Up";
    }

    @NoLoginRequired
    @GetMapping("/id-find")
    public String idFind() {
        return "Id_Find";
    }

    /** 검색 화면. */
    @NoLoginRequired
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("topKeywords", searchKeywordService.getTopKeywords());
        return "Search";
    }

    @NoLoginRequired
    @GetMapping("/main")
    public String main(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                       Model model) {
        // 오늘의 베스트 고민: 좋아요 많은 순 최대 5개, 부족하면 null로 채워 항상 5칸
        List<BestQuestionResponse> bestQuestions = new ArrayList<>(likeService.getBestQuestions(BEST_QUESTION_SLOTS));
        while (bestQuestions.size() < BEST_QUESTION_SLOTS) {
            bestQuestions.add(null);
        }
        model.addAttribute("bestQuestions", bestQuestions);

        // 이달의 멘토: 이번 달 답변 좋아요 많은 순 최대 5명, 부족하면 null로 채워 항상 5칸
        List<BestMentorResponse> bestMentor = new ArrayList<>(likeService.getBestMentors(BEST_MENTOR_SLOTS));
        while (bestMentor.size() < BEST_MENTOR_SLOTS) {
            bestMentor.add(null);
        }
        model.addAttribute("bestMentor", bestMentor);

        // 내가 작성한 게시물 / 최근 게시물 - 실제 사용자 작성 글 기준
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("myPosts", loginUser != null
                ? questionService.getMyQuestions(loginUser.idx(), MY_POST_SLOTS)
                : List.of());
        model.addAttribute("recentPosts", questionService.getRecent(RECENT_POST_SLOTS));

        addNotificationAttributes(loginUser, model);
        return "Main";
    }

    @NoLoginRequired
    @GetMapping("/best-post")
    public String bestPost() {
        return "Best_Post";
    }

    @NoLoginRequired
    @GetMapping("/write")
    public String write() {
        return "Write";
    }

    @GetMapping("/notif")
    public String notif(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                        Model model) {
        model.addAttribute("notifications", notificationService.findAll(loginUser.idx()));
        return "Notif";
    }

    /** 옛 정적 "내 프로필 보기" 링크 호환용 - 실제 프로필 화면으로 리다이렉트. */
    @GetMapping("/user-profile")
    public String userProfile(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser) {
        return "redirect:/users/" + loginUser.idx();
    }

    /** 헤더 알림 드롭다운(최근 5건 + 안 읽은 개수) - 비로그인이면 빈 상태. */
    private void addNotificationAttributes(UserResponse loginUser, Model model) {
        if (loginUser == null) {
            model.addAttribute("recentNotifications", List.of());
            model.addAttribute("unreadNotifCount", 0L);
            return;
        }
        model.addAttribute("recentNotifications", notificationService.findRecent(loginUser.idx()));
        model.addAttribute("unreadNotifCount", notificationService.countUnread(loginUser.idx()));
    }
}
