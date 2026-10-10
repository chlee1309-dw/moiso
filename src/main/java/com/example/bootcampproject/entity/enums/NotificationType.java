package com.example.bootcampproject.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 알림 종류 - 화면에 표시할 아이콘(Font Awesome 클래스)을 함께 들고 있다. */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    ANSWER("fa-solid fa-comment-dots"),
    REPLY("fa-solid fa-reply"),
    LIKE_QUESTION("fa-regular fa-thumbs-up"),
    LIKE_ANSWER("fa-regular fa-thumbs-up"),
    CHAT_MESSAGE("fa-regular fa-comment-dots"),
    APPOINTMENT_REQUESTED("fa-solid fa-calendar-plus"),
    APPOINTMENT_STATUS_CHANGED("fa-solid fa-calendar-check"),
    FRIEND_REQUEST("fa-solid fa-user-plus"),
    FRIEND_ACCEPTED("fa-solid fa-user-check");

    private final String icon;
}
