package com.example.bootcampproject.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 비밀번호 변경
public record PasswordChangeRequest(

        @NotBlank(message = "현재 비밀번호를 입력하세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력하세요.")
        @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력하세요.")
        String newPasswordConfirm
) {

    @AssertTrue(message = "새 비밀번호와 확인 값이 일치하지 않습니다.")
    public boolean isNewPasswordConfirmed() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}
