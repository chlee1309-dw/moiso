package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.AnswerEntity;
import com.example.bootcampproject.entity.AppointmentEntity;
import com.example.bootcampproject.entity.ReviewEntity;
import com.example.bootcampproject.entity.UserEntity;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// 리뷰 작성
public record ReviewCreateRequest(

        @NotNull(message = "별점을 입력하세요.")
        @DecimalMin(value = "0.0", message = "별점은 0.0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다.")
        BigDecimal rating,

        @Size(max = 1000, message = "리뷰는 1000자 이하여야 합니다.")
        String content,

        @NotNull(message = "멘토 정보가 필요합니다.")
        Long mentorIdx,

        Long appointmentIdx,

        Long answerIdx
) {

    @AssertTrue(message = "예약 또는 답변 중 하나에 대해서만 리뷰할 수 있습니다.")
    public boolean isExactlyOneTarget() {
        return (appointmentIdx == null) ^ (answerIdx == null);
    }

    public ReviewEntity toEntity(UserEntity mentee, UserEntity mentor,
                                 AppointmentEntity appointment, AnswerEntity answer) {
        ReviewEntity review = ReviewEntity.builder()
                .rating(rating)
                .content(content)
                .build();
        review.setMentee(mentee);
        review.setMentor(mentor);
        review.setAppointment(appointment);
        review.setAnswer(answer);
        return review;
    }
}
