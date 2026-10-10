package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

// 예약에 따른 상태 변경용
public record AppointmentStatusUpdateRequest(

        @NotNull(message = "변경할 상태를 선택하세요.")
        AppointmentStatus status
) {
}
