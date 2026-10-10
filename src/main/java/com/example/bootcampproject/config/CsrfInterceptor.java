package com.example.bootcampproject.config;

import com.example.bootcampproject.controller.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * GET/HEAD/OPTIONS/TRACE 이외의 요청(POST 등 상태를 바꾸는 요청)은 세션에 저장된 CSRF 토큰과
 * 요청이 들고 온 토큰(폼 hidden input "_csrf" 또는 "X-CSRF-TOKEN" 헤더)이 일치해야 통과한다.
 * 세션 쿠키만으로 인증하는 이 앱은 이 검증이 없으면 외부 사이트가 로그인된 사용자를 대신해
 * 삭제/변경 요청을 위조해 보낼 수 있다 (CSRF). 토큰 발급은 {@link CsrfTokenAdvice} 참고.
 */
public class CsrfInterceptor implements HandlerInterceptor {

    public static final String HEADER_NAME = "X-CSRF-TOKEN";
    public static final String PARAM_NAME = "_csrf";

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (SAFE_METHODS.contains(request.getMethod().toUpperCase())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object sessionToken = session != null ? session.getAttribute(SessionConst.CSRF_TOKEN) : null;

        String requestToken = request.getHeader(HEADER_NAME);
        if (requestToken == null) {
            requestToken = request.getParameter(PARAM_NAME);
        }

        if (sessionToken != null && sessionToken.equals(requestToken)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("잘못된 요청입니다. 페이지를 새로고침한 뒤 다시 시도해 주세요.");
        return false;
    }
}
