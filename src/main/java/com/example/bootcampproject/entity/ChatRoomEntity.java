package com.example.bootcampproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** 두 사용자 사이의 1:1 채팅방. userA/userB는 idx가 작은 쪽이 항상 userA(정렬 고정, 방 중복 생성 방지). */
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "chat_rooms",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_room_users",
                columnNames = {"user_a_idx", "user_b_idx"}))
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_idx")
    private Long idx;

    @CreatedDate
    @Column(name = "chat_room_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_room_user_a"))
    private UserEntity userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_room_user_b"))
    private UserEntity userB;
}
