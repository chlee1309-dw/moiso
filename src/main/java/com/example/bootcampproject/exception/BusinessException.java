package com.example.bootcampproject.exception;

/** 도메인 규칙 위반 예외의 공통 상위 타입. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
