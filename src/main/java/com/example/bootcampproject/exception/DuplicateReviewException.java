package com.example.bootcampproject.exception;

// 동일 대상에 대해 중복 리뷰 작성할 떄
public class DuplicateReviewException extends BusinessException {

    public DuplicateReviewException(String message) {
        super(message);
    }
}
