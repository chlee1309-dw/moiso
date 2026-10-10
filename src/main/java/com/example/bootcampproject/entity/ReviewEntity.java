package com.example.bootcampproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_idx")
    private Long idx;

    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "review_content", columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "review_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 예약 리뷰 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_idx",
            foreignKey = @ForeignKey(name = "fk_review_appointment"))
    private AppointmentEntity appointment;

    // 답변 리뷰 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_idx",
            foreignKey = @ForeignKey(name = "fk_review_answer"))
    private AnswerEntity answer;

    // 멘티
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_idx",
            foreignKey = @ForeignKey(name = "fk_review_mentee"))
    private UserEntity mentee;

    // 멘토
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_idx",
            foreignKey = @ForeignKey(name = "fk_review_mentor"))
    private UserEntity mentor;

    @Builder
    public ReviewEntity(BigDecimal rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
