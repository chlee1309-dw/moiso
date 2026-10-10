package com.example.bootcampproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "question_images")
public class QuestionImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_image_idx")
    private Long idx;

    // 브라우저에서 접근 가능한 경로 (예: /uploads/questions/xxx.jpg)
    @Column(name = "image_path", nullable = false, length = 255)
    private String imagePath;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    // 질문 1
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_question_image_question"))
    private QuestionEntity question;

    @Builder
    public QuestionImageEntity(String imagePath, int displayOrder) {
        this.imagePath = imagePath;
        this.displayOrder = displayOrder;
    }
}
