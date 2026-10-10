package com.example.bootcampproject.config;

import com.example.bootcampproject.controller.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 세션에 로그인 사용자가 없으면 컨트롤러 진입 전에 막는다.
 * {@link NoLoginRequired}가 붙은 컨트롤러/메서드는 예외.
 * 컨트롤러마다 반복되던 "loginUser == null이면 /login으로" 체크를 여기 한 곳으로 모아,
 * 새 엔드포인트를 추가할 때 이 체크를 빠뜨리는 것 자체가 불가능하게 만든다.
 */
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            // 정적 리소스 등 컨트롤러 메서드가 아닌 핸들러는 대상에서 제외
            return true;
        }

        if (isPublic(handlerMethod)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionConst.LOGIN_USER) != null) {
            return true;
        }

        if (isJsonEndpoint(handlerMethod)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }

    private boolean isPublic(HandlerMethod handlerMethod) {
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), NoLoginRequired.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), NoLoginRequired.class);
    }

    private boolean isJsonEndpoint(HandlerMethod handlerMethod) {
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), ResponseBody.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), ResponseBody.class);
    }
}
