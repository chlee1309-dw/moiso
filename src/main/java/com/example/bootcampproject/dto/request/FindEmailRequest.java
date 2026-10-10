package com.example.bootcampproject.dto.request;

import jakarta.validation.constraints.NotBlank;

// 이름 + 학교 + 학과로 가입된 이메일(로그인 아이디) 찾기
public record FindEmailRequest(

        @NotBlank(message = "이름을 입력하세요.")
        String name,

        @NotBlank(message = "학교를 입력하세요.")
        String university,

        @NotBlank(message = "학과를 입력하세요.")
        String department
) {
}
