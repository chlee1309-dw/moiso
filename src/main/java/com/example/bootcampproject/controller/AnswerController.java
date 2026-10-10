package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.request.AnswerCreateRequest;
import com.example.bootcampproject.dto.request.AnswerUpdateRequest;
import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/questions/{questionIdx}/answers")
    public String create(@PathVariable Long questionIdx,
                         @Valid @ModelAttribute AnswerCreateRequest request,
                         BindingResult bindingResult,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "답변 내용을 입력하세요.");
            return "redirect:/questions/" + questionIdx;
        }
        answerService.create(questionIdx, loginUser.idx(), request);
        redirectAttributes.addFlashAttribute("message", "답변을 추가했습니다.");
        return "redirect:/questions/" + questionIdx;
    }

    @PostMapping("/answers/{answerIdx}")
    public String update(@PathVariable Long answerIdx,
                         @RequestParam Long questionIdx,
                         @Valid @ModelAttribute AnswerUpdateRequest request,
                         BindingResult bindingResult,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "답변 내용을 입력하세요.");
            return "redirect:/questions/" + questionIdx;
        }
        answerService.update(answerIdx, loginUser.idx(), request);
        redirectAttributes.addFlashAttribute("message", "답변이 수정되었습니다.");
        return "redirect:/questions/" + questionIdx;
    }

    @PostMapping("/answers/{answerIdx}/delete")
    public String delete(@PathVariable Long answerIdx,
                         @RequestParam Long questionIdx,
                         @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) UserResponse loginUser,
                         RedirectAttributes redirectAttributes) {
        answerService.delete(answerIdx, loginUser.idx());
        redirectAttributes.addFlashAttribute("message", "답변이 삭제되었습니다.");
        return "redirect:/questions/" + questionIdx;
    }
}
