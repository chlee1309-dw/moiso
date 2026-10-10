package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.LocationEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 장소 등록 (관리자)
public record LocationCreateRequest(

        @NotBlank(message = "장소명을 입력하세요.")
        @Size(max = 50, message = "장소명은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "카테고리를 입력하세요.")
        @Size(max = 255, message = "카테고리는 255자 이하여야 합니다.")
        String category,

        String description,

        boolean available
) {

    public LocationEntity toEntity() {
        return LocationEntity.builder()
                .name(name)
                .category(category)
                .description(description)
                .available(available)
                .build();
    }
}
