package com.example.bootcampproject.dto.request;

import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 회원가입
public record SignUpRequest(

        @NotBlank(message = "이름을 입력하세요.")
        @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
        String name,

        @NotBlank(message = "이메일을 입력하세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력하세요.")
        @Size(min = 8, max = 20,message = "비밀번호는 8~64자여야 합니다.")
        String password,

        @NotBlank(message = "비밀번호 확인을 입력하세요.")
        String passwordConfirm,

        @NotNull(message = "가입목적을 선택하세요.")
        Purpose purpose,

        @NotBlank(message = "학교를 입력하세요.")
        @Size(max = 255, message = "학교명이 너무 깁니다.")
        String university,

        @NotBlank(message = "학과를 입력하세요.")
        @Size(max = 255, message = "학과명이 너무 깁니다.")
        String department
) {

    /** 비밀번호 확인 일치 검증. */
    @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(passwordConfirm);
    }

    /** 저장용 엔티티 변환. 비밀번호는 인코딩된 값을 받는다. */
    public UserEntity toEntity(String encodedPassword) {
        return UserEntity.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .purpose(purpose)
                .isVerified(false)
                .university(university)
                .department(department)
                .build();
    }
}
