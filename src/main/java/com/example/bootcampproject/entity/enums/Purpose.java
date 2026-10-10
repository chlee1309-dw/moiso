package com.example.bootcampproject.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 회원가입 시 선택하는 가입목적. */
@Getter
@RequiredArgsConstructor
public enum Purpose {

    COUNSEL("고민상담 목적"),
    SCHOOL("학교생활 목적"),
    CAREER("취업/학업 목적");

    /** 화면 표시용 한글 라벨. */
    private final String label;
}
