package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 친구 요청 / 수락 / 거절 / 목록. */
@Controller
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // 친구 목록 + 받은 요청
    @GetMapping("/friends")
    public String list(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                       Model model) {
        model.addAttribute("friends", friendService.findFriends(loginUser.idx()));
        model.addAttribute("requests", friendService.findIncomingRequests(loginUser.idx()));
        return "Friends";
    }

    // 친구 요청 보내기 (프로필의 "친구 추가" 버튼)
    @PostMapping("/friends/request")
    public String request(@RequestParam Long counterpartIdx,
                          @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request) {
        friendService.request(loginUser.idx(), counterpartIdx);
        redirectAttributes.addFlashAttribute("message", "친구 요청을 보냈어요.");
        return "redirect:" + refererOr(request, "/friends");
    }

    @PostMapping("/friends/{idx}/accept")
    public String accept(@PathVariable Long idx,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        friendService.accept(idx, loginUser.idx());
        redirectAttributes.addFlashAttribute("message", "친구 요청을 수락했어요.");
        return "redirect:/friends";
    }

    @PostMapping("/friends/{idx}/reject")
    public String reject(@PathVariable Long idx,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        friendService.reject(idx, loginUser.idx());
        redirectAttributes.addFlashAttribute("message", "친구 요청을 거절했어요.");
        return "redirect:/friends";
    }

    @PostMapping("/friends/{idx}/remove")
    public String remove(@PathVariable Long idx,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        friendService.remove(idx, loginUser.idx());
        redirectAttributes.addFlashAttribute("message", "친구를 삭제했어요.");
        return "redirect:/friends";
    }

    private String refererOr(HttpServletRequest request, String fallback) {
        String referer = request.getHeader("Referer");
        return referer != null ? referer : fallback;
    }
}
