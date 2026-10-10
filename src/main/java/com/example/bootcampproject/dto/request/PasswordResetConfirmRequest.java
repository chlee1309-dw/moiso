package com.example.bootcampproject.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 이메일로 받은 링크에서 새 비밀번호 설정
public record PasswordResetConfirmRequest(

        @NotBlank(message = "잘못된 접근입니다.")
        String token,

        @NotBlank(message = "새 비밀번호를 입력하세요.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
        String newPassword,

        @NotBlank(message = "비밀번호 확인을 입력하세요.")
        String newPasswordConfirm
) {

    @AssertTrue(message = "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}
