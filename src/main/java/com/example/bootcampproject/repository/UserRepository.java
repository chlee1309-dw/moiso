package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 이메일로 사용자 확인
    Optional<UserEntity> findByEmail(String email);

    // 캘린더 구독(webcal) 토큰으로 사용자 확인
    Optional<UserEntity> findByCalendarToken(String calendarToken);

    // 이메일(아이디) 찾기 - 이름+학교+학과 일치 확인. 동명이인이 있을 수 있어 List로 받아 서비스에서 단건 검증
    List<UserEntity> findByNameAndUniversityAndDepartment(String name, String university, String department);
}
