package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.response.NotificationResponse;
import com.example.bootcampproject.entity.NotificationEntity;
import com.example.bootcampproject.entity.enums.NotificationType;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.NotificationRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 알림 생성/조회/읽음 처리. 다른 서비스(답변, 좋아요, 예약, 채팅)에서 이벤트 발생 시 {@link #notify}를 호출한다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int LIST_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 알림 생성. 행위자(actorIdx)와 수신자가 같으면(자기 자신에게 일어난 일) 조용히 무시
    @Transactional
    public void notify(Long recipientIdx, Long actorIdx, NotificationType type, String content, String link) {
        if (recipientIdx == null || recipientIdx.equals(actorIdx)) {
            return;
        }
        NotificationEntity notification = NotificationEntity.builder()
                .type(type)
                .content(content)
                .link(link)
                .build();
        notification.setRecipient(userRepository.getReferenceById(recipientIdx));
        notificationRepository.save(notification);
    }

    // 알림 드롭다운(최근 5건)
    public List<NotificationResponse> findRecent(Long userIdx) {
        return notificationRepository.findTop5ByRecipient_IdxOrderByCreatedAtDesc(userIdx).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    // 전체 알림 목록 화면
    public List<NotificationResponse> findAll(Long userIdx) {
        return notificationRepository.findByRecipient_IdxOrderByCreatedAtDesc(userIdx, PageRequest.of(0, LIST_PAGE_SIZE))
                .map(NotificationResponse::from)
                .getContent();
    }

    public long countUnread(Long userIdx) {
        return notificationRepository.countByRecipient_IdxAndIsReadFalse(userIdx);
    }

    // 알림을 읽음 처리하고 이동할 링크를 반환 (본인 알림이 아니면 없는 것처럼 처리)
    @Transactional
    public String markReadAndGetLink(Long notificationIdx, Long userIdx) {
        NotificationEntity notification = notificationRepository.findByIdxAndRecipient_Idx(notificationIdx, userIdx)
                .orElseThrow(() -> new ResourceNotFoundException("알림", notificationIdx));
        notification.setRead(true);
        return notification.getLink();
    }

    @Transactional
    public void markAllRead(Long userIdx) {
        notificationRepository.markAllRead(userIdx);
    }
}
