package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.CalendarEventEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

// 개인 일정 직접 추가
public record CalendarEventCreateRequest(

        @NotNull(message = "날짜를 선택하세요.")
        LocalDate date,

        // 시간 미입력 시 종일 일정으로 처리
        LocalTime time,

        @NotBlank(message = "일정 제목을 입력하세요.")
        @Size(max = 100, message = "일정 제목은 100자 이하여야 합니다.")
        String title
) {

    public CalendarEventEntity toEntity() {
        return CalendarEventEntity.builder()
                .date(date)
                .time(time)
                .title(title)
                .build();
    }
}
