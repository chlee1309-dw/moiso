package com.example.bootcampproject.exception;

/** 입력한 비밀번호가 저장된 값과 다를 때. */
public class InvalidPasswordException extends BusinessException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
