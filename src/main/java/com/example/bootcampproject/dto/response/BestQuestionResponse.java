package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.enums.QuestionCategory;

import java.time.LocalDateTime;

public record BestQuestionResponse(
        Long idx,
        QuestionCategory category,
        String title,
        String writerName,
        LocalDateTime createdAt,
        long likeCount
) {
    public static BestQuestionResponse from(QuestionEntity question, long likeCount) {
        return new BestQuestionResponse(
                question.getIdx(),
                question.getCategory(),
                question.getTitle(),
                question.getWriter().getName(),
                question.getCreatedAt(),
                likeCount
        );
    }
}
