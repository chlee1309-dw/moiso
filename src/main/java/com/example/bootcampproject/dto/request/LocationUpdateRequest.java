package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.LocationEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 장소 수정 (관리자)
public record LocationUpdateRequest(

        @NotBlank(message = "장소명을 입력하세요.")
        @Size(max = 50, message = "장소명은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "카테고리를 입력하세요.")
        @Size(max = 255, message = "카테고리는 255자 이하여야 합니다.")
        String category,

        String description,

        boolean available
) {

    public void applyTo(LocationEntity location) {
        location.setName(name);
        location.setCategory(category);
        location.setDescription(description);
        location.setAvailable(available);
    }
}
