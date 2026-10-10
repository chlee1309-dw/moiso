package com.example.bootcampproject.dto.request;

import jakarta.validation.constraints.NotBlank;

// 지도에서 클릭/검색해 고른 장소를 등록 장소로 변환할 때 사용
public record LocationResolveRequest(

        @NotBlank(message = "장소명이 필요합니다.")
        String name,

        String category,

        String address,

        Double lat,

        Double lng
) {
}
