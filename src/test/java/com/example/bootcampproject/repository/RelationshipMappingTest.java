package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.*;
import com.example.bootcampproject.entity.enums.AppointmentStatus;
import com.example.bootcampproject.entity.enums.Purpose;
import com.example.bootcampproject.entity.enums.QuestionCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** 전체 FK 연관관계를 실제로 저장하며 확인한다. */
@SpringBootTest
class RelationshipMappingTest {

    @Autowired UserRepository userRepository;
    @Autowired LocationRepository locationRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired AnswerRepository answerRepository;
    @Autowired AppointmentRepository appointmentRepository;
    @Autowired ReviewRepository reviewRepository;

    private UserEntity newUser(String type) {
        return userRepository.save(UserEntity.builder()
                .name(type)
                .email(type.toLowerCase() + "-" + System.nanoTime() + "@example.com")
                .password("test-password")
                .type(type)
                .purpose(Purpose.CAREER)
                .university("Example University")
                .department("Computer Science")
                .isVerified(true)
                .build());
    }

    @Transactional
    @Test
    void wholeGraph() {
        var mentee = newUser("MENTEE");
        var mentor = newUser("MENTOR");

        var location = locationRepository.save(LocationEntity.builder()
                .name("Gangnam Center")
                .category("OFFICE")
                .description("강남역 5번 출구 스터디룸")
                .available(true)
                .build());

        // appointment -> mentee, mentor, location
        var appointment = AppointmentEntity.builder()
                .date(LocalDate.now().plusDays(3))
                .time(LocalTime.of(14, 30))
                .status(AppointmentStatus.REQUESTED)
                .build();
        appointment.setMentee(mentee);
        appointment.setMentor(mentor);
        appointment.setLocation(location);
        appointmentRepository.save(appointment);

        // question -> writer
        var question = QuestionEntity.builder()
                .category(QuestionCategory.WORRY_CAREER)
                .title("이직 관련 질문")
                .content("연관관계 통합 테스트용 질문 본문")
                .build();
        question.setWriter(mentee);
        questionRepository.save(question);

        // answer -> question, writer ; reply -> parent
        var answer = AnswerEntity.builder().content("이직은 이렇게 준비하세요.").build();
        answer.setQuestion(question);
        answer.setWriter(mentor);
        answerRepository.save(answer);

        var reply = AnswerEntity.builder().content("추가로 궁금한 점이 있습니다.").build();
        reply.setQuestion(question);
        reply.setWriter(mentee);
        reply.setParent(answer);
        answerRepository.save(reply);

        // review on appointment
        var appointmentReview = ReviewEntity.builder()
                .rating(new BigDecimal("4.50"))
                .content("멘토링 예약이 유익했습니다.")
                .build();
        appointmentReview.setAppointment(appointment);
        appointmentReview.setMentee(mentee);
        appointmentReview.setMentor(mentor);
        reviewRepository.save(appointmentReview);

        // review on answer
        var answerReview = ReviewEntity.builder()
                .rating(new BigDecimal("5.00"))
                .content("답변이 정확했습니다.")
                .build();
        answerReview.setAnswer(answer);
        answerReview.setMentee(mentee);
        answerReview.setMentor(mentor);
        reviewRepository.save(answerReview);

        assertNotNull(appointment.getIdx());
        assertEquals(location.getIdx(), appointment.getLocation().getIdx());
        assertEquals(mentee.getIdx(), appointment.getMentee().getIdx());
        assertEquals(mentor.getIdx(), appointment.getMentor().getIdx());
        assertEquals(question.getIdx(), answer.getQuestion().getIdx());
        assertEquals(answer.getIdx(), reply.getParent().getIdx());
        assertEquals(appointment.getIdx(), appointmentReview.getAppointment().getIdx());
        assertEquals(answer.getIdx(), answerReview.getAnswer().getIdx());
        assertNull(answerReview.getAppointment());
    }
}
