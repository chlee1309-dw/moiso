package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.AnswerLikeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AnswerLikeRepository extends JpaRepository<AnswerLikeEntity, Long> {

    // 사용자가 해당 답변에 좋아요를 눌렀는지 (중복 체크 / 버튼 상태)
    boolean existsByUser_IdxAndAnswer_Idx(Long userIdx, Long answerIdx);

    // 좋아요 취소
    long deleteByUser_IdxAndAnswer_Idx(Long userIdx, Long answerIdx);

    // 답변별 좋아요 수
    long countByAnswer_Idx(Long answerIdx);

    // 여러 답변의 좋아요 수 일괄 조회 (질문 상세 화면 N+1 방지) - row[0]=answerIdx, row[1]=count
    @Query("select l.answer.idx, count(l) from AnswerLikeEntity l " +
            "where l.answer.idx in :answerIdxs group by l.answer.idx")
    List<Object[]> countByAnswerIdxIn(@Param("answerIdxs") Collection<Long> answerIdxs);

    // 답변 목록에서 "내가 좋아요한 답변" 한 번에 조회 (N+1 방지)
    @Query("select l.answer.idx from AnswerLikeEntity l " +
            "where l.user.idx = :userIdx and l.answer.idx in :answerIdxs")
    List<Long> findLikedAnswerIdxs(@Param("userIdx") Long userIdx,
                                   @Param("answerIdxs") Collection<Long> answerIdxs);

    // 이달의 멘토 (기간 내 답변 좋아요를 많이 받은 답변 작성자 top N) - row[0]=writerIdx, row[1]=count
    @Query("select l.answer.writer.idx, count(l) from AnswerLikeEntity l " +
            "where l.createdAt >= :start and l.createdAt < :end " +
            "group by l.answer.writer.idx order by count(l) desc")
    List<Object[]> findBestMentorIdxBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            Pageable pageable);
}
