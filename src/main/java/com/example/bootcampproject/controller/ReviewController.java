package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.request.ReviewCreateRequest;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 완료된 예약 / 답변에 대한 멘토 리뷰 작성. */
@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public String create(@Valid @ModelAttribute ReviewCreateRequest request,
                         BindingResult bindingResult,
                         @RequestParam(required = false) Long questionIdx,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        // 답변에 대한 리뷰는 질문 상세로, 예약에 대한 리뷰는 예약 목록으로 돌아간다.
        String redirectUrl = questionIdx != null ? "redirect:/questions/" + questionIdx : "redirect:/appointments";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "별점을 선택하고, 예약 또는 답변 중 하나만 리뷰 대상으로 지정해 주세요.");
            return redirectUrl;
        }
        reviewService.create(loginUser.idx(), request);
        redirectAttributes.addFlashAttribute("message", "리뷰를 남겼습니다.");
        return redirectUrl;
    }
}
