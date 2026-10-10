package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.UserEntity;

// 다른 사용자에게 보여주는 공개 프로필
public record UserPublicProfileResponse(
        Long idx,
        String name,
        String university,
        String department,
        long questionCount,
        long answerCount
) {

    public static UserPublicProfileResponse of(UserEntity user, long questionCount, long answerCount) {
        return new UserPublicProfileResponse(
                user.getIdx(),
                user.getName(),
                user.getUniversity(),
                user.getDepartment(),
                questionCount,
                answerCount
        );
    }
}
