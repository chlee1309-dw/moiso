package com.example.bootcampproject.entity;

import com.example.bootcampproject.entity.enums.FriendStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** 친구 요청/관계. requester가 addressee에게 요청을 보내고, addressee가 수락하면 ACCEPTED. */
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "friends",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_friend_requester_addressee",
                columnNames = {"requester_idx", "addressee_idx"}))
public class FriendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_idx")
    private Long idx;

    @Enumerated(EnumType.STRING)
    @Column(name = "friend_status", nullable = false, length = 20)
    private FriendStatus status;

    @CreatedDate
    @Column(name = "friend_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_friend_requester"))
    private UserEntity requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addressee_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_friend_addressee"))
    private UserEntity addressee;
}
