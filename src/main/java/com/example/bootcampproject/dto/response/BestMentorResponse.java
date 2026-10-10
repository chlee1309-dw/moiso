package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.UserEntity;

public record BestMentorResponse(
        Long idx,
        String name,
        long likeCount
) {
    public static BestMentorResponse from(UserEntity user, long likeCount) {
        return new BestMentorResponse(
                user.getIdx(),
                user.getName(),
                likeCount
        );
    }
}
