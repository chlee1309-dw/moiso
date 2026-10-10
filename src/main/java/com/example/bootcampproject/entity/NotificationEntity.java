package com.example.bootcampproject.entity;

import com.example.bootcampproject.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_idx")
    private Long idx;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "notification_content", nullable = false, length = 255)
    private String content;

    // 클릭 시 이동할 경로 (예: /questions/3)
    @Column(name = "notification_link", nullable = false, length = 255)
    private String link;

    @Column(name = "notification_is_read", nullable = false)
    private boolean isRead;

    @CreatedDate
    @Column(name = "notification_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_recipient"))
    private UserEntity recipient;

    @Builder
    public NotificationEntity(NotificationType type, String content, String link) {
        this.type = type;
        this.content = content;
        this.link = link;
        this.isRead = false;
    }
}
