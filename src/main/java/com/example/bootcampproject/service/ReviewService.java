package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.request.ReviewCreateRequest;
import com.example.bootcampproject.dto.response.ReviewResponse;
import com.example.bootcampproject.entity.AnswerEntity;
import com.example.bootcampproject.entity.AppointmentEntity;
import com.example.bootcampproject.entity.ReviewEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.AppointmentStatus;
import com.example.bootcampproject.exception.BusinessException;
import com.example.bootcampproject.exception.DuplicateReviewException;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.AnswerRepository;
import com.example.bootcampproject.repository.AppointmentRepository;
import com.example.bootcampproject.repository.ReviewRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final AnswerRepository answerRepository;

    // 리뷰 작성 및 생성된 리뷰 idx반환
    @Transactional
    public Long create(Long menteeIdx, ReviewCreateRequest request) {
        if ((request.appointmentIdx() == null) == (request.answerIdx() == null)) {
            throw new BusinessException("예약 또는 답변 중 하나만 리뷰 대상으로 지정해야 합니다.");
        }

        UserEntity mentee = userRepository.findById(menteeIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", menteeIdx));
        UserEntity mentor = userRepository.findById(request.mentorIdx())
                .orElseThrow(() -> new ResourceNotFoundException("멘토", request.mentorIdx()));

        if (mentee.getIdx().equals(mentor.getIdx())) {
            throw new BusinessException("자기 자신에게 리뷰를 남길 수 없습니다.");
        }

        AppointmentEntity appointment = null;
        AnswerEntity answer = null;

        if (request.appointmentIdx() != null) {
            appointment = appointmentRepository.findById(request.appointmentIdx())
                    .orElseThrow(() -> new ResourceNotFoundException("예약", request.appointmentIdx()));

            if (appointment.getStatus() != AppointmentStatus.DONE) {
                throw new BusinessException("완료된 예약에만 리뷰를 남길 수 있습니다.");
            }
            if (!appointment.getMentee().getIdx().equals(menteeIdx)) {
                throw new ForbiddenException("본인이 멘티로 참여한 예약만 리뷰할 수 있습니다.");
            }
            if (!appointment.getMentor().getIdx().equals(mentor.getIdx())) {
                throw new BusinessException("예약의 멘토와 리뷰 대상 멘토가 일치하지 않습니다.");
            }
            if (reviewRepository.existsByAppointment_Idx(appointment.getIdx())) {
                throw new DuplicateReviewException("이미 이 예약에 리뷰를 남겼습니다.");
            }
        } else {
            answer = answerRepository.findById(request.answerIdx())
                    .orElseThrow(() -> new ResourceNotFoundException("답변", request.answerIdx()));

            if (!answer.getWriter().getIdx().equals(mentor.getIdx())) {
                throw new BusinessException("답변 작성자와 리뷰 대상 멘토가 일치하지 않습니다.");
            }
            if (!answer.getQuestion().getWriter().getIdx().equals(menteeIdx)) {
                throw new ForbiddenException("본인이 작성한 질문에 달린 답변만 리뷰할 수 있습니다.");
            }
            if (reviewRepository.existsByAnswer_IdxAndMentee_Idx(answer.getIdx(), menteeIdx)) {
                throw new DuplicateReviewException("이미 이 답변에 리뷰를 남겼습니다.");
            }
        }

        ReviewEntity saved = reviewRepository.save(
                request.toEntity(mentee, mentor, appointment, answer));
        return saved.getIdx();
    }

    // 멘토가 받은 리뷰
    public List<ReviewResponse> getMentorReviews(Long mentorIdx) {
        return reviewRepository.findByMentor_IdxOrderByCreatedAtDesc(mentorIdx)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    // 멘토가 받은 별점(리뷰 총점)
    public double getMentorAverageRating(Long mentorIdx) {
        Double average = reviewRepository.findAverageRatingByMentorIdx(mentorIdx);
        return average != null ? average : 0.0;
    }

    private static final double BASE_TEMPERATURE = 36.5;
    private static final double RATING_WEIGHT = 5.0;

    public double getMentorTemperature(Long mentorIdx) {
        Double average = reviewRepository.findAverageRatingByMentorIdx(mentorIdx);
        if (average == null) {
            return BASE_TEMPERATURE;
        }

        double temperature = BASE_TEMPERATURE + (average - 2.5) * RATING_WEIGHT;
        return Math.max(0, Math.min(100,temperature));
    }

    // 이 예약에 이미 리뷰를 남겼는지
    public boolean hasReviewed(Long appointmentIdx) {
        return reviewRepository.existsByAppointment_Idx(appointmentIdx);
    }

    // 로그인한 멘티가 이미 리뷰를 남긴 답변 idx 목록 (질문 상세 화면에서 답변별 버튼 노출용)
    public Set<Long> findReviewedAnswerIdxs(Long menteeIdx, Collection<Long> answerIdxs) {
        if (menteeIdx == null || answerIdxs == null || answerIdxs.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(reviewRepository.findReviewedAnswerIdxsByMentee(menteeIdx, answerIdxs));
    }
}
