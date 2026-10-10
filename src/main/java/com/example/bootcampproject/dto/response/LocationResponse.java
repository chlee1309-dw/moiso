package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.LocationEntity;

// 장소 출력용
public record LocationResponse(
        Long idx,
        String name,
        String category,
        String description,
        Boolean available,
        String address,
        Double lat,
        Double lng
) {

    public static LocationResponse from(LocationEntity location) {
        return new LocationResponse(
                location.getIdx(),
                location.getName(),
                location.getCategory(),
                location.getDescription(),
                location.getAvailable(),
                location.getAddress(),
                location.getLat(),
                location.getLng()
        );
    }
}
