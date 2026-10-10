package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.enums.QuestionCategory;

import java.time.LocalDateTime;

// 질문 목록 출력용
public record QuestionListItemResponse(
        Long idx,
        QuestionCategory category,
        String title,
        String writerName,
        LocalDateTime createdAt,
        long answerCount,
        long likeCount,
        String thumbnailUrl
) {

    public static QuestionListItemResponse from(QuestionEntity question, long answerCount, long likeCount,
                                                String thumbnailUrl) {
        return new QuestionListItemResponse(
                question.getIdx(),
                question.getCategory(),
                question.getTitle(),
                question.getWriter().getName(),
                question.getCreatedAt(),
                answerCount,
                likeCount,
                thumbnailUrl
        );
    }
}
