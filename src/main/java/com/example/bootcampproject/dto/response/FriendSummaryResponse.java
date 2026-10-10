package com.example.bootcampproject.dto.response;

// 내 친구 목록 한 줄
public record FriendSummaryResponse(
        Long friendIdx,
        Long counterpartIdx,
        String counterpartName,
        String university,
        String department
) {
}
