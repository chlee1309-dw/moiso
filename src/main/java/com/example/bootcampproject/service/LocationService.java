package com.example.bootcampproject.service;

import com.example.bootcampproject.dto.request.LocationCreateRequest;
import com.example.bootcampproject.dto.request.LocationResolveRequest;
import com.example.bootcampproject.dto.request.LocationUpdateRequest;
import com.example.bootcampproject.dto.response.LocationResponse;
import com.example.bootcampproject.entity.LocationEntity;
import com.example.bootcampproject.exception.ResourceNotFoundException;
import com.example.bootcampproject.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;

    // 장소 등록 및 장소 idx 반환
    @Transactional
    public Long create(LocationCreateRequest request) {
        return locationRepository.save(request.toEntity()).getIdx();
    }

    // 장소 수정
    @Transactional
    public void update(Long locationIdx, LocationUpdateRequest request) {
        LocationEntity location = getLocationOrThrow(locationIdx);
        request.applyTo(location);
    }

    // 장소 삭제
    @Transactional
    public void delete(Long locationIdx) {
        LocationEntity location = getLocationOrThrow(locationIdx);
        locationRepository.delete(location);
    }

    // 예약 가능한 장소 목록
    public List<LocationResponse> getAvailable() {
        return locationRepository.findByAvailableTrue()
                .stream()
                .map(LocationResponse::from)
                .toList();
    }

    /** 전체 장소 목록 (관리자 화면). */
    public List<LocationResponse> getAll() {
        return locationRepository.findAll()
                .stream()
                .map(LocationResponse::from)
                .toList();
    }

    /** 예약 신청 지도에서 고른 장소를 찾거나(같은 주소가 이미 있으면 재사용) 새로 등록한다. */
    @Transactional
    public LocationResponse findOrCreateByPlace(LocationResolveRequest request) {
        String address = request.address();
        if (address != null && !address.isBlank()) {
            var existing = locationRepository.findByAddress(address);
            if (existing.isPresent()) {
                return LocationResponse.from(existing.get());
            }
        }

        String category = (request.category() == null || request.category().isBlank())
                ? "카카오맵 검색"
                : request.category();
        category = category.length() > 255 ? category.substring(0, 255) : category;
        String name = request.name().length() > 50 ? request.name().substring(0, 50) : request.name();

        LocationEntity created = LocationEntity.builder()
                .name(name)
                .category(category)
                .address(address)
                .lat(request.lat())
                .lng(request.lng())
                .available(true)
                .build();
        return LocationResponse.from(locationRepository.save(created));
    }

    private LocationEntity getLocationOrThrow(Long locationIdx) {
        return locationRepository.findById(locationIdx)
                .orElseThrow(() -> new ResourceNotFoundException("장소", locationIdx));
    }
}
