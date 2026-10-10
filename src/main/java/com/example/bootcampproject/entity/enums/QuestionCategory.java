package com.example.bootcampproject.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 질문 게시판 카테고리. {@code /questions?category=...} 필터링에 사용. */
@Getter
@RequiredArgsConstructor
public enum QuestionCategory {

    TODAY("오늘의 대학생활"),
    FREE("자유게시판"),
    WORRY_CAREER("고민상담소 · 진로/취업"),
    WORRY_STUDY("고민상담소 · 학업/성적"),
    WORRY_RELATION("고민상담소 · 연애/인간관계"),
    MENTOR_FIND("멘토링 게시판 · 멘토 찾기"),
    MENTOR_REVIEW("멘토링 게시판 · 멘토링 후기"),
    STUDY("스터디 모임"),
    MARKET("중고 장터");

    /** 화면 표시용 한글 라벨. */
    private final String label;
}
