package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    // 멘티로 참여한 예약 목록
    @Query("select a from AppointmentEntity a " +
            "join fetch a.mentee join fetch a.mentor join fetch a.location " +
            "where a.mentee.idx = :menteeIdx order by a.date desc, a.time desc")
    List<AppointmentEntity> findByMenteeIdxWithDetails(@Param("menteeIdx") Long menteeIdx);

    // 멘토로 참여한 예약 목록
    @Query("select a from AppointmentEntity a " +
            "join fetch a.mentee join fetch a.mentor join fetch a.location " +
            "where a.mentor.idx = :mentorIdx order by a.date desc, a.time desc")
    List<AppointmentEntity> findByMentorIdxWithDetails(@Param("mentorIdx") Long mentorIdx);
}
