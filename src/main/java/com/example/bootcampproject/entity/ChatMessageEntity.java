package com.example.bootcampproject.entity;

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
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_idx")
    private Long idx;

    @Column(name = "chat_message_content", nullable = false, length = 1000)
    private String content;

    @CreatedDate
    @Column(name = "chat_message_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_message_room"))
    private ChatRoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_message_sender"))
    private UserEntity sender;

    @Builder
    public ChatMessageEntity(String content) {
        this.content = content;
    }
}
