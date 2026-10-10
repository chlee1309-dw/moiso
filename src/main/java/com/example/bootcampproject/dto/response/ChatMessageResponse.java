package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.ChatMessageEntity;

import java.time.LocalDateTime;

// 채팅 메시지 출력용
public record ChatMessageResponse(
        Long idx,
        Long senderIdx,
        String senderName,
        String content,
        LocalDateTime createdAt
) {

    public static ChatMessageResponse from(ChatMessageEntity message) {
        return new ChatMessageResponse(
                message.getIdx(),
                message.getSender().getIdx(),
                message.getSender().getName(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
