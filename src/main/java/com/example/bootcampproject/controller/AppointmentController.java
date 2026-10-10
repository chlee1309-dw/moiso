package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.request.AppointmentCreateRequest;
import com.example.bootcampproject.dto.request.AppointmentStatusUpdateRequest;
import com.example.bootcampproject.dto.response.AppointmentResponse;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.entity.enums.AppointmentStatus;
import com.example.bootcampproject.service.AppointmentService;
import com.example.bootcampproject.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//멘토링 예약 신청/조회/상태변경
@Controller
@RequiredArgsConstructor
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ReviewService reviewService;

    // 내 예약 목록
    @GetMapping
    public String list(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                       Model model) {
        List<AppointmentResponse> asMentee = appointmentService.findAsMentee(loginUser.idx());
        model.addAttribute("asMentee", asMentee);
        model.addAttribute("asMentor", appointmentService.findAsMentor(loginUser.idx()));
        model.addAttribute("loginUserIdx", loginUser.idx());

        // 완료된 예약 중 이미 리뷰를 남겼는지 (멘티 화면의 "리뷰 남기기" 버튼 노출용)
        Map<Long, Boolean> reviewed = new HashMap<>();
        for (AppointmentResponse a : asMentee) {
            if (a.status() == AppointmentStatus.DONE) {
                reviewed.put(a.idx(), reviewService.hasReviewed(a.idx()));
            }
        }
        model.addAttribute("reviewed", reviewed);
        return "appointment/list";
    }

    //예약신청 폼
    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long mentorIdx,
                             @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                             Model model) {
        model.addAttribute("mentorIdx", mentorIdx);
        return "appointment/form";
    }

    // 예약 신청 처리
    @PostMapping
    public String create(@Valid @ModelAttribute AppointmentCreateRequest request,
                         BindingResult bindingResult,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "멘토 · 장소 · 날짜 · 시간을 모두 입력해 주세요. (날짜는 내일 이후)");
            return "redirect:/appointments/new"
                    + (request.mentorIdx() != null ? "?mentorIdx=" + request.mentorIdx() : "");
        }
        appointmentService.create(loginUser.idx(), request);
        redirectAttributes.addFlashAttribute("message", "예약을 신청했어요. 멘토의 수락을 기다려 주세요.");
        return "redirect:/appointments";
    }

    // 예약 상태 변경 (수락/거절/완료/취소)
    @PostMapping("/{idx}/status")
    public String changeStatus(@PathVariable Long idx,
                               @Valid @ModelAttribute AppointmentStatusUpdateRequest request,
                               BindingResult bindingResult,
                               @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "변경할 상태가 올바르지 않아요.");
            return "redirect:/appointments";
        }
        appointmentService.changeStatus(idx, loginUser.idx(), request);
        redirectAttributes.addFlashAttribute("message", "예약 상태를 변경했어요.");
        return "redirect:/appointments";
    }
}
