package com.example.bootcampproject.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** 사용자가 직접 추가하는 개인 일정 (예약과 별개). */
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "calendar_events")
public class CalendarEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_event_idx")
    private Long idx;

    @Column(name = "calendar_event_date", nullable = false)
    private LocalDate date;

    @Column(name = "calendar_event_time")
    private LocalTime time;

    @Column(name = "calendar_event_title", nullable = false, length = 100)
    private String title;

    @CreatedDate
    @Column(name = "calendar_event_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_calendar_event_user"))
    private UserEntity user;

    @Builder
    public CalendarEventEntity(LocalDate date, LocalTime time, String title) {
        this.date = date;
        this.time = time;
        this.title = title;
    }
}
