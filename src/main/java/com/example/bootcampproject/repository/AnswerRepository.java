package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {

    // 질문 상세 답변
    @Query("select a from AnswerEntity a join fetch a.writer " +
            "where a.question.idx = :questionIdx order by a.createdAt asc")
    List<AnswerEntity> findByQuestionIdxWithWriter(@Param("questionIdx") Long questionIdx);

    // 질문별 답변 수
    long countByQuestion_Idx(Long questionIdx);

    // 사용자가 남긴 답변 수
    long countByWriter_Idx(Long writerIdx);
}
