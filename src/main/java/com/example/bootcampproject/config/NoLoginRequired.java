package com.example.bootcampproject.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인 없이 접근 가능한 컨트롤러/메서드에 붙인다.
 * 붙이지 않으면 {@link LoginCheckInterceptor}가 기본적으로 로그인을 요구한다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoLoginRequired {
}
