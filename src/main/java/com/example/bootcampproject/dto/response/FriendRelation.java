package com.example.bootcampproject.dto.response;

/** 두 사용자 사이의 친구 관계 (프로필 화면의 버튼 상태 결정용). DB에 저장되지 않는 조회 전용 값. */
public enum FriendRelation {
    NONE,
    REQUESTED_BY_ME,
    REQUESTED_BY_THEM,
    FRIENDS
}
