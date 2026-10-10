package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.AppointmentEntity;
import com.example.bootcampproject.entity.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// 예약 출력용
public record AppointmentResponse(
        Long idx,
        LocalDate date,
        LocalTime time,
        AppointmentStatus status,
        Long menteeIdx,
        String menteeName,
        Long mentorIdx,
        String mentorName,
        Long locationIdx,
        String locationName,
        LocalDateTime createdAt
) {

    public static AppointmentResponse from(AppointmentEntity appointment) {
        return new AppointmentResponse(
                appointment.getIdx(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getStatus(),
                appointment.getMentee().getIdx(),
                appointment.getMentee().getName(),
                appointment.getMentor().getIdx(),
                appointment.getMentor().getName(),
                appointment.getLocation().getIdx(),
                appointment.getLocation().getName(),
                appointment.getCreatedAt()
        );
    }
}
