package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.enums.QuestionCategory;

import java.time.LocalDateTime;
import java.util.List;

// 질문 상세화면 출력용
public record QuestionDetailResponse(
        Long idx,
        QuestionCategory category,
        String title,
        String content,
        Long writerIdx,
        String writerName,
        LocalDateTime createdAt,
        long likeCount,
        boolean likedByMe,
        // 최상위 답변만 담고, 대댓글은 각 AnswerResponse.replies()에 중첩되어 있다.
        List<AnswerResponse> answers,
        long answerCount,
        List<String> imageUrls
) {

    public static QuestionDetailResponse from(QuestionEntity question, List<AnswerResponse> answers,
                                              long answerCount, List<String> imageUrls,
                                              long likeCount, boolean likedByMe) {
        return new QuestionDetailResponse(
                question.getIdx(),
                question.getCategory(),
                question.getTitle(),
                question.getContent(),
                question.getWriter().getIdx(),
                question.getWriter().getName(),
                question.getCreatedAt(),
                likeCount,
                likedByMe,
                answers,
                answerCount,
                imageUrls
        );
    }
}
