package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.AnswerEntity;
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
class AnswerRepositoryTest {

    @Autowired
    AnswerRepository answerRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional
    @Test
    void saveAnswer() {
        var user = userRepository.save(UserEntity.builder()
                .name("Answer Writer")
                .email("a-writer-" + System.nanoTime() + "@example.com")
                .password("test-password")
                .type("MENTOR")
                .purpose(Purpose.CAREER)
                .university("Example University")
                .department("Computer Science")
                .isVerified(true)
                .build());

        var question = QuestionEntity.builder()
                .category(QuestionCategory.FREE)
                .title("답변 매핑 확인용 질문")
                .content("답변 연관관계 확인용 질문 내용")
                .build();
        question.setWriter(user);
        questionRepository.save(question);

        var answer = AnswerEntity.builder()
                .content("이렇게 하시면 됩니다. 연관관계 확인용 답변 내용입니다.")
                .build();
        answer.setQuestion(question);
        answer.setWriter(user);

        var saved = answerRepository.save(answer);

        assertNotNull(saved.getIdx());
        assertNotNull(saved.getCreatedAt());
        assertEquals(question.getIdx(), saved.getQuestion().getIdx());
        assertNull(saved.getParent());

        // 대답변(자기참조) 확인
        var reply = AnswerEntity.builder().content("대답변입니다.").build();
        reply.setQuestion(question);
        reply.setWriter(user);
        reply.setParent(saved);
        var savedReply = answerRepository.save(reply);

        assertEquals(saved.getIdx(), savedReply.getParent().getIdx());
    }
}
