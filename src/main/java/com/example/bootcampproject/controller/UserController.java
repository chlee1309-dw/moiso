package com.example.bootcampproject.controller;

import com.example.bootcampproject.config.NoLoginRequired;
import com.example.bootcampproject.dto.request.LoginRequest;
import com.example.bootcampproject.dto.request.PasswordChangeRequest;
import com.example.bootcampproject.dto.request.ProfileUpdateRequest;
import com.example.bootcampproject.dto.request.SignUpRequest;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.service.FriendService;
import com.example.bootcampproject.service.ReviewService;
import com.example.bootcampproject.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final FriendService friendService;

    @NoLoginRequired
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signUpRequest") SignUpRequest request,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "Sing_Up";
        }
        userService.signup(request);
        return "redirect:/login";
    }

    @NoLoginRequired
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                        BindingResult bindingResult,
                        @RequestParam(defaultValue = "/main") String redirectURL,
                        HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "Login";
        }
        UserResponse user = userService.login(request);
        session.setAttribute(SessionConst.LOGIN_USER, user);
        return "redirect:" + redirectURL;
    }

    @NoLoginRequired
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /** 내 프로필 화면. */
    @GetMapping("/profile")
    public String profilePage(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                              Model model) {
        model.addAttribute("user", userService.getProfile(loginUser.idx()));
        model.addAttribute("temperature", reviewService.getMentorTemperature(loginUser.idx()));
        return "Profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileUpdateRequest request,
                                BindingResult bindingResult,
                                @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "학교 · 학과 · 가입목적을 모두 입력해 주세요.");
            return "redirect:/profile";
        }
        UserResponse updated = userService.updateProfile(loginUser.idx(), request);
        session.setAttribute(SessionConst.LOGIN_USER, updated);
        redirectAttributes.addFlashAttribute("message", "프로필이 수정되었습니다.");
        return "redirect:/profile";
    }

    /** 다른 사용자의 공개 프로필 (질문/답변에서 이름을 눌러 진입). */
    @GetMapping("/users/{idx}")
    public String publicProfile(@PathVariable Long idx,
                                @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                                Model model) {
        model.addAttribute("profile", userService.getPublicProfile(idx));
        model.addAttribute("isSelf", loginUser.idx().equals(idx));
        model.addAttribute("reviews", reviewService.getMentorReviews(idx));
        model.addAttribute("avgRating", reviewService.getMentorAverageRating(idx));
        model.addAttribute("temperature", reviewService.getMentorTemperature(idx));
        model.addAttribute("friendRelation", friendService.getRelation(loginUser.idx(), idx));

        return "User_Profile";
    }

    /** 비밀번호 변경 화면 (마이페이지에서 분리). */
    @GetMapping("/profile/password")
    public String passwordPage() {
        return "Password_Change";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeRequest request,
                                 BindingResult bindingResult,
                                 @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "새 비밀번호는 8자 이상이어야 하며, 확인 값과 일치해야 합니다.");
            return "redirect:/profile/password";
        }
        userService.changePassword(loginUser.idx(), request);
        redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        return "redirect:/profile";
    }
}
