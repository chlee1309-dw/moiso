package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;
import com.example.bootcampproject.entity.enums.QuestionCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuestionRepositoryTest {

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional
    @Test
    void saveQuestion() {
        var writer = userRepository.save(UserEntity.builder()
                .name("Question Writer")
                .email("q-writer-" + System.nanoTime() + "@example.com")
                .password("test-password")
                .type("MENTEE")
                .purpose(Purpose.CAREER)
                .university("Example University")
                .department("Computer Science")
                .isVerified(true)
                .build());

        var question = QuestionEntity.builder()
                .category(QuestionCategory.FREE)
                .title("스프링 부트 질문입니다")
                .content("JPA 연관관계가 잘 매핑되는지 확인용 질문 내용입니다.")
                .build();
        question.setWriter(writer);

        var saved = questionRepository.save(question);

        assertNotNull(saved.getIdx());
        assertNotNull(saved.getCreatedAt());
        assertEquals(writer.getIdx(), saved.getWriter().getIdx());
    }
}
