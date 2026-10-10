package com.example.bootcampproject.entity;

import com.example.bootcampproject.entity.enums.QuestionCategory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_idx")
    private Long idx;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_category", length = 30)
    private QuestionCategory category;

    @Column(name = "question_title", nullable = false, length = 100)
    private String title;

    @Column(name = "question_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "question_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_question_user"))
    private UserEntity writer;

    @Builder
    public QuestionEntity(QuestionCategory category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }
}
