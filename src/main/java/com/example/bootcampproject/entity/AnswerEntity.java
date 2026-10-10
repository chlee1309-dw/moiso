package com.example.bootcampproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "answers")
public class AnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_idx")
    private Long idx;

    @Column(name = "answer_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "answer_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 질문 1
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_answer_question"))
    private QuestionEntity question;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_answer_user"))
    private UserEntity writer;

    // 상위 답변 (대댓글, 없으면 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_answer_idx",
            foreignKey = @ForeignKey(name = "fk_answer_parent"))
    private AnswerEntity parent;

    @Builder
    public AnswerEntity(String content) {
        this.content = content;
    }
}
