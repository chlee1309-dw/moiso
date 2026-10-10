package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    // 멘토가 받은 리뷰 목록
    /** 멘토가 받은 리뷰 목록 (최신순). */
    List<ReviewEntity> findByMentor_IdxOrderByCreatedAtDesc(Long mentorIdx);

    // 멘토 평균 평점
    @Query("select avg(r.rating) from ReviewEntity r where r.mentor.idx = :mentorIdx")
    Double findAverageRatingByMentorIdx(@Param("mentorIdx") Long mentorIdx);

    // 예약에 대한 리뷰 중복 확인
    boolean existsByAppointment_Idx(Long appointmentIdx);

    // 답변에 대한 리뷰 중복 확인
    boolean existsByAnswer_IdxAndMentee_Idx(Long answerIdx, Long menteeIdx);

    // 로그인한 멘티가 이미 리뷰를 남긴 답변 idx 목록 (여러 답변을 한 번에 조회)
    @Query("select r.answer.idx from ReviewEntity r where r.mentee.idx = :menteeIdx and r.answer.idx in :answerIdxs")
    List<Long> findReviewedAnswerIdxsByMentee(@Param("menteeIdx") Long menteeIdx,
                                               @Param("answerIdxs") Collection<Long> answerIdxs);
}
