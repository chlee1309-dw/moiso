package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest (

        @NotBlank(message = "학교를 입력하세요.")
    @Size(max = 255, message = "학교명은 255자 이하여야 합니다.")
    String university,

        @NotBlank(message = "학과를 입력하세요.")
    @Size(max = 255, message = "학과명은 최대 255자 이하여야 합니다.")
    String department,

        @NotNull(message = "가입목적을 선택하세요.")
        Purpose purpose
) {
    public void applyTo(UserEntity user) {
        user.setUniversity(university);
        user.setDepartment(department);
        user.setPurpose(purpose);
    }
}
