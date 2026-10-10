package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.request.QuestionCreateRequest;
import com.example.bootcampproject.dto.request.QuestionUpdateRequest;
import com.example.bootcampproject.dto.response.QuestionDetailResponse;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.entity.enums.QuestionCategory;
import com.example.bootcampproject.exception.ForbiddenException;
import com.example.bootcampproject.service.QuestionService;
import com.example.bootcampproject.service.SearchKeywordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final SearchKeywordService searchKeywordService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) QuestionCategory category,
                       @PageableDefault(size = 10) Pageable pageable,
                       Model model) {
        searchKeywordService.record(keyword);
        model.addAttribute("questions", questionService.getList(category, keyword, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("categories", QuestionCategory.values());
        return "question/list";
    }

    @GetMapping("/{idx}")
    public String detail(@PathVariable Long idx,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         Model model) {
        Long loginUserIdx = (loginUser != null) ? loginUser.idx() : null;
        model.addAttribute("question", questionService.getDetail(idx, loginUserIdx));
        model.addAttribute("loginUserIdx", loginUserIdx);
        return "question/detail";
    }

    @GetMapping("/new")
    public String createForm(@SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                             Model model) {
        model.addAttribute("questionForm", new QuestionCreateRequest(null, null, null));
        model.addAttribute("questionIdx", null);
        return "Write";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("questionForm") QuestionCreateRequest request,
                         BindingResult bindingResult,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("questionIdx", null);
            return "Write";
        }
        Long idx = questionService.create(loginUser.idx(), request, images);
        redirectAttributes.addFlashAttribute("message", "질문이 등록되었습니다.");
        return "redirect:/questions/" + idx;
    }

    @GetMapping("/{idx}/edit")
    public String editForm(@PathVariable Long idx,
                           @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                           Model model) {
        QuestionDetailResponse question = questionService.getDetail(idx, loginUser.idx());
        if (!question.writerIdx().equals(loginUser.idx())) {
            throw new ForbiddenException("본인이 작성한 질문만 수정할 수 있습니다.");
        }
        model.addAttribute("questionForm",
                new QuestionUpdateRequest(question.category(), question.title(), question.content()));
        model.addAttribute("questionIdx", idx);
        model.addAttribute("existingImages", question.imageUrls());
        return "Write";
    }

    @PostMapping("/{idx}")
    public String update(@PathVariable Long idx,
                         @Valid @ModelAttribute("questionForm") QuestionUpdateRequest request,
                         BindingResult bindingResult,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("questionIdx", idx);
            return "Write";
        }
        questionService.update(idx, loginUser.idx(), request, images);
        redirectAttributes.addFlashAttribute("message", "질문이 수정되었습니다.");
        return "redirect:/questions/" + idx;
    }

    @PostMapping("/{idx}/delete")
    public String delete(@PathVariable Long idx,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        questionService.delete(idx, loginUser.idx());
        redirectAttributes.addFlashAttribute("message", "질문이 삭제되었습니다.");
        return "redirect:/questions";
    }
}
