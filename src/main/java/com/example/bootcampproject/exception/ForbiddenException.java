package com.example.bootcampproject.exception;

// 권한이 없는 행위를 실행할때
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message);
    }
}
