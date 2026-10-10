package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.CalendarEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, Long> {

    // 내가 등록한 개인 일정 전체
    List<CalendarEventEntity> findByUser_Idx(Long userIdx);
}
