package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.QuestionImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface QuestionImageRepository extends JpaRepository<QuestionImageEntity, Long> {

    // 질문 상세 - 표시 순서대로
    List<QuestionImageEntity> findByQuestion_IdxOrderByDisplayOrderAsc(Long questionIdx);

    // 질문에 이미 첨부된 사진 수 (최대 개수 제한용)
    long countByQuestion_Idx(Long questionIdx);

    // 질문 삭제 시 함께 삭제
    void deleteByQuestion_Idx(Long questionIdx);

    // 목록 화면 썸네일용 - 질문별 첫 번째(순서 0) 사진만 한 번에 조회 (N+1 방지)
    @Query("select qi.question.idx, qi.imagePath from QuestionImageEntity qi " +
            "where qi.question.idx in :questionIdxs and qi.displayOrder = 0")
    List<Object[]> findFirstImagesByQuestionIdxs(@Param("questionIdxs") Collection<Long> questionIdxs);
}
