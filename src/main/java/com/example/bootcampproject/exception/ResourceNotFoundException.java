package com.example.bootcampproject.exception;

// id로 조회한 엔티티가 없는 경우
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // 질문을 찾을 수 없을 때
    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + "(idx=" + id + ")을(를) 찾을 수 없습니다.");
    }
}
