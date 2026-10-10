package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.QuestionEntity;
import com.example.bootcampproject.entity.enums.QuestionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    // 메인 질문 목록들
    Page<QuestionEntity> findByOrderByCreatedAtDesc(Pageable pageable);

    // 제목 검색
    Page<QuestionEntity> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    // 카테고리 목록
    Page<QuestionEntity> findByCategoryOrderByCreatedAtDesc(QuestionCategory category, Pageable pageable);

    // 카테고리 + 제목
    Page<QuestionEntity> findByCategoryAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            QuestionCategory category, String keyword, Pageable pageable);

    // 특정 사용자가 작성한 질문
    List<QuestionEntity> findByWriter_IdxOrderByCreatedAtDesc(Long writerIdx);

    // 회원별 질문 수
    long countByWriter_Idx(Long writerIdx);

    // 작성자까지 나오는 상세 조회
    @Query("select q from QuestionEntity q join fetch q.writer where q.idx = :idx")
    Optional<QuestionEntity> findDetailByIdx(@Param("idx") Long idx);

    @Query("select  q from QuestionEntity q join fetch q.writer where q.idx in :idxs")
    List<QuestionEntity> findAllWithWriterByIdxIn(@Param("idxs") Collection<Long> idxs);
}
