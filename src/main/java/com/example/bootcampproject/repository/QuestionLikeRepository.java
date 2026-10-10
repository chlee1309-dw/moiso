package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.QuestionLikeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface QuestionLikeRepository extends JpaRepository<QuestionLikeEntity, Long> {

    // 사용자가 해당 질문에 좋아요를 눌렀는지 (중복 체크 / 버튼 상태)
    boolean existsByUser_IdxAndQuestion_Idx(Long userIdx, Long questionIdx);

    // 좋아요 취소 (삭제된 행 수 반환)
    long deleteByUser_IdxAndQuestion_Idx(Long userIdx, Long questionIdx);

    // 질문별 좋아요 수
    long countByQuestion_Idx(Long questionIdx);

    // 여러 질문의 좋아요 수 일괄 조회 (목록 화면 N+1 방지) - row[0]=questionIdx, row[1]=count
    @Query("select l.question.idx, count(l) from QuestionLikeEntity l " +
            "where l.question.idx in :questionIdxs group by l.question.idx")
    List<Object[]> countByQuestionIdxIn(@Param("questionIdxs") Collection<Long> questionIdxs);

    // 질문 목록에서 "내가 좋아요한 질문" 한 번에 조회 (N+1 방지)
    @Query("select l.question.idx from QuestionLikeEntity l " +
            "where l.user.idx = :userIdx and l.question.idx in :questionIdxs")
    List<Long> findLikedQuestionIdxs(@Param("userIdx") Long userIdx,
                                     @Param("questionIdxs") Collection<Long> questionIdxs);

    // 오늘의 베스트 고민 (기간 내 좋아요를 많이 받은 질문 top N)
    @Query("select l.question.idx, count(l) from QuestionLikeEntity l " +
            "where l.createdAt >= :start and l.createdAt < :end " +
            "group by l.question.idx order by count(l) desc")
    List<Object[]> findBestQuestionIdxBetween(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              Pageable pageable);
}
