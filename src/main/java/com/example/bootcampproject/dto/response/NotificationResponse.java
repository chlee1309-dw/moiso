package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.NotificationEntity;
import com.example.bootcampproject.entity.enums.NotificationType;

import java.time.LocalDateTime;

// 알림 출력용
public record NotificationResponse(
        Long idx,
        NotificationType type,
        String content,
        String link,
        boolean read,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(NotificationEntity n) {
        return new NotificationResponse(
                n.getIdx(),
                n.getType(),
                n.getContent(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
