package com.example.bootcampproject.entity;

import com.example.bootcampproject.entity.enums.Purpose;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Setter
@ToString
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "user_email"),
                @UniqueConstraint(name = "uk_users_calendar_token", columnNames = "user_calendar_token")
        }
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    private Long idx;

    @Column(name = "user_name", nullable = false, length = 20)
    private String name;

    @Column(name = "user_email", nullable = false, length = 50)
    private String email;

    @Column(name = "user_password", nullable = false, length = 100)
    private String password;

    /** 고정 멘토/멘티 구분 없음 - 역할은 질문/답변 작성 행위로 파생됨. */
    @Column(name = "user_type", length = 50)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_purpose", nullable = false, length = 50)
    private Purpose purpose;

    @Column(name = "user_is_verified")
    private Boolean isVerified;

    @Column(name = "user_university", nullable = false, length = 255)
    private String university;

    @Column(name = "user_department", nullable = false, length = 255)
    private String department;

    @Column(name = "user_calendar_token", length = 40)
    private String calendarToken;

    @CreatedDate
    @Column(name = "user_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;



    @Builder
    public UserEntity(String name, String email, String password, String type, Purpose purpose,
                      Boolean isVerified, String university, String department) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.type = type;
        this.purpose = purpose;
        this.isVerified = isVerified;
        this.university = university;
        this.department = department;
    }
}
