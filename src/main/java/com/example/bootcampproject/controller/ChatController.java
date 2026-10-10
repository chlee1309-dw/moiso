package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.response.ChatMessageResponse;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.service.ChatService;
import com.example.bootcampproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

/** 1:1 채팅 목록 / 채팅방 / 메시지 송수신(폴링). */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final NotificationService notificationService;

    // 내 채팅 목록
    @GetMapping("/chat")
    public String list(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                       Model model) {
        model.addAttribute("rooms", chatService.findMyRooms(loginUser.idx()));
        model.addAttribute("recentNotifications", notificationService.findRecent(loginUser.idx()));
        model.addAttribute("unreadNotifCount", notificationService.countUnread(loginUser.idx()));
        return "Chat";
    }

    // 상대방과의 채팅방을 찾거나 새로 만들어 이동 (프로필의 "1:1 채팅하기" 버튼)
    @PostMapping("/chats/start")
    public String start(@RequestParam Long counterpartIdx,
                        @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser) {
        Long roomIdx = chatService.getOrCreateRoom(loginUser.idx(), counterpartIdx);
        return "redirect:/chats/" + roomIdx;
    }

    // 채팅방 화면
    @GetMapping("/chats/{roomIdx}")
    public String room(@PathVariable Long roomIdx,
                       @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                       Model model) {
        model.addAttribute("roomIdx", roomIdx);
        model.addAttribute("counterpart", chatService.getCounterpart(roomIdx, loginUser.idx()));
        model.addAttribute("messages", chatService.getMessages(roomIdx, loginUser.idx()));
        model.addAttribute("loginUserIdx", loginUser.idx());
        return "Chat_Room";
    }

    // 폴링용 메시지 조회 (afterId 이후 새 메시지만, 없으면 전체)
    @GetMapping("/chats/{roomIdx}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageResponse>> messages(
            @PathVariable Long roomIdx,
            @RequestParam(required = false) Long afterId,
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser) {
        List<ChatMessageResponse> result = afterId == null
                ? chatService.getMessages(roomIdx, loginUser.idx())
                : chatService.getMessagesAfter(roomIdx, loginUser.idx(), afterId);
        return ResponseEntity.ok(result);
    }

    // 메시지 전송
    @PostMapping("/chats/{roomIdx}/messages")
    @ResponseBody
    public ResponseEntity<ChatMessageResponse> send(
            @PathVariable Long roomIdx,
            @RequestParam String content,
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser) {
        return ResponseEntity.ok(chatService.sendMessage(roomIdx, loginUser.idx(), content));
    }
}
