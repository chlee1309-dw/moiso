package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.request.CalendarEventCreateRequest;
import com.example.bootcampproject.entity.CalendarEventEntity;
import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.CalendarEventRepository;
import com.example.bootcampproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 사용자가 직접 추가하는 개인 일정. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;

    // 일정 추가, idx를 반환
    @Transactional
    public Long create(Long userIdx, CalendarEventCreateRequest request) {
        UserEntity user = userRepository.findById(userIdx)
                .orElseThrow(() -> new ResourceNotFoundException("회원", userIdx));

        CalendarEventEntity event = request.toEntity();
        event.setUser(user);
        return calendarEventRepository.save(event).getIdx();
    }

    // 내 개인 일정 전체
    public List<CalendarEventEntity> findMine(Long userIdx) {
        return calendarEventRepository.findByUser_Idx(userIdx);
    }

    // 일정 삭제 (본인 것만)
    @Transactional
    public void delete(Long eventIdx, Long userIdx) {
        CalendarEventEntity event = calendarEventRepository.findById(eventIdx)
                .orElseThrow(() -> new ResourceNotFoundException("일정", eventIdx));
        if (!event.getUser().getIdx().equals(userIdx)) {
            throw new ForbiddenException("본인이 등록한 일정만 삭제할 수 있습니다.");
        }
        calendarEventRepository.delete(event);
    }
}
