package com.example.bootcampproject.exception;

import com.example.bootcampproject.entity.enums.AppointmentStatus;

// 예약 상태를 변경할 때
public class InvalidStatusTransitionException extends BusinessException {

    public InvalidStatusTransitionException(AppointmentStatus from, AppointmentStatus to) {
        super(from.getLabel() + " 상태에서 " + to.getLabel() + " 상태로 변경할 수 없습니다.");
    }
}
