package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.AnswerEntity;
import jakarta.validation.constraints.NotBlank;

// 답변 수정용
public record AnswerUpdateRequest(

        @NotBlank(message = "답변 내용을 입력하세요.")
        String content
) {

    public void applyTo(AnswerEntity answer) {
        answer.setContent(content);
    }
}
