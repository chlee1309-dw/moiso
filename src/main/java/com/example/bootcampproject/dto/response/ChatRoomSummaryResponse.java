package com.example.bootcampproject.dto.response;

import java.time.LocalDateTime;

// 채팅 목록 화면 한 줄(상대방 + 마지막 메시지)
public record ChatRoomSummaryResponse(
        Long roomIdx,
        Long counterpartIdx,
        String counterpartName,
        String lastMessage,
        LocalDateTime lastMessageAt
) {
}
