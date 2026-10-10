package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

/** 알림 읽음 처리 (목록/드롭다운 공용). */
@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 클릭 -> 읽음 처리 후 알림이 가리키는 화면으로 이동
    @PostMapping("/notifications/{idx}/open")
    public String open(@PathVariable Long idx,
                       @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser) {
        String link = notificationService.markReadAndGetLink(idx, loginUser.idx());
        return "redirect:" + link;
    }

    // 모두 읽음
    @PostMapping("/notifications/read-all")
    public String readAll(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                          HttpServletRequest request) {
        notificationService.markAllRead(loginUser.idx());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/notif");
    }
}
