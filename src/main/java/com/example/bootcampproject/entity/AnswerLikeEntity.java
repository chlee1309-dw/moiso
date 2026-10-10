package com.example.bootcampproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "answer_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_answer_like_user_answer",
                columnNames = {"user_idx", "answer_idx"}))
public class AnswerLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_like_idx")
    private Long idx;

    @CreatedDate
    @Column(name = "answer_like_created_at", nullable = false,
    updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_answer_like_user"))
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_answer_like_answer"))
    private AnswerEntity answer;

}
