package com.example.bootcampproject.controller;

import com.example.bootcampproject.config.NoLoginRequired;
import com.example.bootcampproject.dto.request.FindEmailRequest;
import com.example.bootcampproject.dto.request.PasswordResetConfirmRequest;
import com.example.bootcampproject.dto.request.PasswordResetRequestRequest;
import com.example.bootcampproject.service.PasswordResetService;
import com.example.bootcampproject.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 이메일(아이디) 찾기 / 비밀번호 재설정. */
@NoLoginRequired
@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    // 이름+학교+학과로 가입 이메일 찾기
    @PostMapping("/id-find/email")
    public String findEmail(@Valid @ModelAttribute FindEmailRequest request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "이름 · 학교 · 학과를 모두 입력해 주세요.");
            return "redirect:/id-find";
        }
        String maskedEmail = userService.findMaskedEmail(request.name(), request.university(), request.department());
        redirectAttributes.addFlashAttribute("foundEmail", maskedEmail);
        return "redirect:/id-find";
    }

    // 비밀번호 재설정 메일 발송 요청
    @PostMapping("/id-find/password")
    public String requestPasswordReset(@Valid @ModelAttribute PasswordResetRequestRequest request,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "올바른 이메일을 입력해 주세요.");
            return "redirect:/id-find";
        }
        passwordResetService.requestReset(request.email(), baseUrl(httpRequest) + "/reset-password");
        redirectAttributes.addFlashAttribute("message",
                "가입된 이메일이라면 비밀번호 재설정 링크를 보냈어요. 메일함을 확인해 주세요.");
        return "redirect:/id-find";
    }

    // 메일 속 링크로 진입 - 새 비밀번호 입력 화면
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            return "redirect:/id-find";
        }
        model.addAttribute("token", token);
        return "Reset_Password";
    }

    // 새 비밀번호 적용
    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute PasswordResetConfirmRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호는 8자 이상이어야 하며, 확인 값과 일치해야 합니다.");
            return "redirect:/reset-password?token=" + request.token();
        }
        passwordResetService.resetPassword(request.token(), request.newPassword());
        redirectAttributes.addFlashAttribute("message", "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해 주세요.");
        return "redirect:/login";
    }

    private String baseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}
