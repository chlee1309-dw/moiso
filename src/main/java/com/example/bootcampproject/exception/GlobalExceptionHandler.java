package com.example.bootcampproject.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * 화면(Thymeleaf) 엔드포인트에서 온 예외는 이전 페이지로 리다이렉트 + 플래시 메시지로,
 * JSON(@ResponseBody) 엔드포인트(채팅 폴링 등)에서 온 예외는 상태코드 + JSON 바디로 응답한다.
 * {@link BusinessException} 이외의 예상 못한 예외까지 잡아서, 사용자에게 스택트레이스가 그대로
 * 노출되는 Whitelabel Error Page 대신 일관된 안내 메시지를 보여준다.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Object handleBusiness(BusinessException e, HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        log.debug("도메인 규칙 위반: {}", e.getMessage());
        if (isJsonEndpoint(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:" + refererOr(request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleUploadTooLarge(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.debug("업로드 용량 초과");
        String message = "첨부 파일 용량이 너무 큽니다. (파일당 최대 10MB, 전체 30MB)";
        if (isJsonEndpoint(request)) {
            return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(Map.of("message", message));
        }
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:" + refererOr(request);
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpected(Exception e, HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        log.error("예상하지 못한 오류: {} {}", request.getMethod(), request.getRequestURI(), e);
        String message = "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
        if (isJsonEndpoint(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", message));
        }
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:" + refererOr(request);
    }

    /** 이 요청을 처리하던 핸들러(메서드 또는 클래스)에 @ResponseBody가 붙어 있으면 JSON 엔드포인트로 본다. */
    private boolean isJsonEndpoint(HttpServletRequest request) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), ResponseBody.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), ResponseBody.class);
    }

    private String refererOr(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return referer != null ? referer : "/main";
    }
}
