package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.enums.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 질문 수정
public record QuestionUpdateRequest(

        QuestionCategory category,

        @NotBlank(message = "제목을 입력하세요.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용을 입력하세요.")
        String content
) {

    public void applyTo(QuestionEntity question) {
        question.setCategory(category);
        question.setTitle(title);
        question.setContent(content);
    }
}
