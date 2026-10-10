package com.example.bootcampproject.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 예약 상태: REQUESTED → ACCEPTED → DONE, 또는 REJECTED/CANCELLED. */
@Getter
@RequiredArgsConstructor
public enum AppointmentStatus {

    REQUESTED("예약 요청"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    DONE("완료"),
    CANCELLED("취소됨");

    /** 화면 표시용 한글 라벨. */
    private final String label;

    /** 다음 상태 집합. */
    public Set<AppointmentStatus> nextStates() {
        return switch (this) {
            case REQUESTED -> Set.of(ACCEPTED, REJECTED, CANCELLED);
            case ACCEPTED -> Set.of(DONE, CANCELLED);
            case REJECTED, DONE, CANCELLED -> Set.of();
        };
    }

    /** {@code target} 으로 전이 가능 여부. */
    public boolean canTransitionTo(AppointmentStatus target) {
        return nextStates().contains(target);
    }
}
