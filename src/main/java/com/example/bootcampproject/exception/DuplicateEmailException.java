package com.example.bootcampproject.exception;

// 이미 사용중인 이메일로 가입할 때
public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException(String email) {
        super("이미 가입된 이메일입니다: " + email);
    }
}
