package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.request.LocationCreateRequest;
import com.example.bootcampproject.dto.request.LocationResolveRequest;
import com.example.bootcampproject.dto.response.LocationResponse;
import com.example.bootcampproject.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 장소 관리 (관리자용). */
@Controller
@RequiredArgsConstructor
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("locations", locationService.getAll());
        return "location/list";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute LocationCreateRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "장소명과 카테고리는 필수예요.");
            return "redirect:/locations";
        }
        locationService.create(request);
        redirectAttributes.addFlashAttribute("message", "장소가 등록되었습니다.");
        return "redirect:/locations";
    }

    @PostMapping("/{idx}/delete")
    public String delete(@PathVariable Long idx, RedirectAttributes redirectAttributes) {
        locationService.delete(idx);
        redirectAttributes.addFlashAttribute("message", "장소가 삭제되었습니다.");
        return "redirect:/locations";
    }

    // 예약 신청 지도에서 고른 장소를 등록 장소(locationIdx)로 변환. 같은 주소가 이미 있으면 재사용.
    @PostMapping("/resolve")
    @ResponseBody
    public LocationResponse resolve(@Valid @RequestBody LocationResolveRequest request) {
        return locationService.findOrCreateByPlace(request);
    }
}
