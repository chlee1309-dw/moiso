package com.example.bootcampproject.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 친구 관계 상태: REQUESTED(요청됨) -> ACCEPTED(친구). */
@Getter
@RequiredArgsConstructor
public enum FriendStatus {

    REQUESTED("요청됨"),
    ACCEPTED("친구");

    private final String label;
}
