package com.example.bootcampproject.entity;

import com.example.bootcampproject.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "appointments")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_idx")
    private Long idx;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate date;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_status", length = 255)
    private AppointmentStatus status;

    @CreatedDate
    @Column(name = "appointment_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 멘티
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentee_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_appointment_mentee"))
    private UserEntity mentee;

    // 멘토
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_appointment_mentor"))
    private UserEntity mentor;

    // 장소
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_idx", nullable = false,
            foreignKey = @ForeignKey(name = "fk_appointment_location"))
    private LocationEntity location;

    @Builder
    public AppointmentEntity(LocalDate date, LocalTime time, AppointmentStatus status) {
        this.date = date;
        this.time = time;
        this.status = status;
    }
}
