package com.example.bootcampproject.entity;

import jakarta.persistence.*;
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
@Table(name = "question_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_question_like_user_question",
                columnNames = {"user_idx", "question_idx"}))
public class QuestionLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_like_idx")
    private Long idx;

    @CreatedDate
    @Column(name = "question_like_created_at", nullable = false,
    updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_question_like_user"))
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_question_like_question"))
    private QuestionEntity question;
}
