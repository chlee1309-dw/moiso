package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.AnswerEntity;
import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.UserEntity;
import jakarta.validation.constraints.NotBlank;

// 댓글용, parent는 대댓글
public record AnswerCreateRequest(

        @NotBlank(message = "답변 내용을 입력하세요.")
        String content,

        Long parentAnswerIdx
) {

    public AnswerEntity toEntity(QuestionEntity question, UserEntity writer, AnswerEntity parent) {
        AnswerEntity answer = AnswerEntity.builder()
                .content(content)
                .build();
        answer.setQuestion(question);
        answer.setWriter(writer);
        answer.setParent(parent);
        return answer;
    }
}
