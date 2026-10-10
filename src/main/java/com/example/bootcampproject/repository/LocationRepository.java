package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    // 예약 시 선택할 수 있는 장소 목록
    List<LocationEntity> findByAvailableTrue();

    // 카카오맵에서 고른 장소가 이미 등록돼 있는지 (주소 기준 중복 방지)
    Optional<LocationEntity> findByAddress(String address);
}
