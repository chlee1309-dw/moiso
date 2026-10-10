package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.UserEntity;
import com.example.bootcampproject.entity.enums.Purpose;

import java.time.LocalDateTime;

// 유저 정보 출력용
public record UserResponse(
        Long idx,
        String name,
        String email,
        Purpose purpose,
        String university,
        String department,
        boolean verified,
        LocalDateTime createdAt
) {

    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getIdx(),
                user.getName(),
                user.getEmail(),
                user.getPurpose(),
                user.getUniversity(),
                user.getDepartment(),

                Boolean.TRUE.equals(user.getIsVerified()),
                user.getCreatedAt()
                );
}

}
