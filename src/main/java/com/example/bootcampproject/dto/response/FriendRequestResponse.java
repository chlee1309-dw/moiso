package com.example.bootcampproject.dto.response;

import java.time.LocalDateTime;

// 내가 받은 친구 요청 한 줄
public record FriendRequestResponse(
        Long friendIdx,
        Long requesterIdx,
        String requesterName,
        LocalDateTime createdAt
) {
}
