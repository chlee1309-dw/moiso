package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 질문 작성
public record QuestionCreateRequest(

        QuestionCategory category,

        @NotBlank(message = "제목을 입력하세요.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용을 입력하세요.")
        String content
) {

    public QuestionEntity toEntity(UserEntity writer) {
        QuestionEntity question = QuestionEntity.builder()
                .category(category)
                .title(title)
                .content(content)
                .build();
        question.setWriter(writer);
        return question;
    }
}
