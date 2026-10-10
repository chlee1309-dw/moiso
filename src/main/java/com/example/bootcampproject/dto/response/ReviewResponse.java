package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.ReviewEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 리뷰 출력용
public record ReviewResponse(
        Long idx,
        BigDecimal rating,
        String content,
        Long menteeIdx,
        String menteeName,
        Long mentorIdx,
        String mentorName,
        Long appointmentIdx,
        Long answerIdx,
        LocalDateTime createdAt
) {

    public static ReviewResponse from(ReviewEntity review) {
        return new ReviewResponse(
                review.getIdx(),
                review.getRating(),
                review.getContent(),
                review.getMentee() != null ? review.getMentee().getIdx() : null,
                review.getMentee() != null ? review.getMentee().getName() : null,
                review.getMentor() != null ? review.getMentor().getIdx() : null,
                review.getMentor() != null ? review.getMentor().getName() : null,
                review.getAppointment() != null ? review.getAppointment().getIdx() : null,
                review.getAnswer() != null ? review.getAnswer().getIdx() : null,
                review.getCreatedAt()
        );
    }
}
