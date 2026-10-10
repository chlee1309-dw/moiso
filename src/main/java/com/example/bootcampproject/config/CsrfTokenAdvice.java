package com.example.bootcampproject.config;

import com.example.bootcampproject.controller.SessionConst;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

/**
 * 화면을 렌더링하는 모든 요청에 CSRF 토큰을 모델로 실어 보낸다 (세션당 1개, 없으면 새로 발급).
 * 템플릿은 {@code <meta name="_csrf" th:content="${csrfToken}">}로 값을 내려받고,
 * static/js/csrf.js가 그 값을 폼 hidden input과 fetch 헤더에 자동으로 채워 넣는다.
 */
@ControllerAdvice
public class CsrfTokenAdvice {

    @ModelAttribute("csrfToken")
    public String csrfToken(HttpSession session) {
        Object token = session.getAttribute(SessionConst.CSRF_TOKEN);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(SessionConst.CSRF_TOKEN, token);
        }
        return (String) token;
    }
}
