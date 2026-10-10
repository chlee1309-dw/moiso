package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.AppointmentEntity;
import com.example.bootcampproject.entity.LocationEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

// 대면 예약 신청용
public record AppointmentCreateRequest(

        @NotNull(message = "멘토를 선택하세요.")
        Long mentorIdx,

        @NotNull(message = "장소를 선택하세요.")
        Long locationIdx,

        @NotNull(message = "날짜를 선택하세요.")
        @Future(message = "예약 날짜는 오늘 이후여야 합니다.")
        LocalDate date,

        @NotNull(message = "시간을 선택하세요.")
        LocalTime time
) {

    public AppointmentEntity toEntity(UserEntity mentee, UserEntity mentor, LocationEntity location) {
        AppointmentEntity appointment = AppointmentEntity.builder()
                .date(date)
                .time(time)
                .status(AppointmentStatus.REQUESTED)
                .build();
        appointment.setMentee(mentee);
        appointment.setMentor(mentor);
        appointment.setLocation(location);
        return appointment;
    }
}
