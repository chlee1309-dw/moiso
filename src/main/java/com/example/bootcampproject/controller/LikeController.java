package com.example.bootcampproject.controller;

import com.example.bootcampproject.dto.response.UserResponse;
import com.example.bootcampproject.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/questions/{questionIdx}/like")
    public String toggleQuestionLike(@PathVariable Long questionIdx,
                                     @SessionAttribute(name = SessionConst.LOGIN_USER, required = false)
                                     UserResponse loginUser) {
        likeService.toggleQuestionLike(loginUser.idx(), questionIdx);
        return  "redirect:/questions/" + questionIdx;
    }

    @PostMapping("/answers/{answerIdx}/like")
    public String toggleAnswerLike(@PathVariable Long answerIdx,
                                   @RequestParam Long questionIdx,
                                   @SessionAttribute(name = SessionConst.LOGIN_USER, required = false)
                                   UserResponse loginUser) {
        likeService.toggleAnswerLike(loginUser.idx(), answerIdx);
        return  "redirect:/questions/" + questionIdx;
    }
}
