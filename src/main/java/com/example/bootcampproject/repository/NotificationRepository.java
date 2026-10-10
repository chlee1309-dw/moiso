package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // 알림 드롭다운(최근 N건) 용
    List<NotificationEntity> findTop5ByRecipient_IdxOrderByCreatedAtDesc(Long recipientIdx);

    // 전체 알림 목록 화면용
    Page<NotificationEntity> findByRecipient_IdxOrderByCreatedAtDesc(Long recipientIdx, Pageable pageable);

    long countByRecipient_IdxAndIsReadFalse(Long recipientIdx);

    // 읽음 처리 권한 확인용 - 본인 알림만
    Optional<NotificationEntity> findByIdxAndRecipient_Idx(Long idx, Long recipientIdx);

    @Modifying
    @Query("update NotificationEntity n set n.isRead = true " +
            "where n.recipient.idx = :recipientIdx and n.isRead = false")
    int markAllRead(@Param("recipientIdx") Long recipientIdx);
}
