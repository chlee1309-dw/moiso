package com.example.bootcampproject.dto.response;

import com.example.bootcampproject.entity.AnswerEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 답변 출력용
public record AnswerResponse(
        Long idx,
        String content,
        Long writerIdx,
        String writerName,
        Long parentAnswerIdx,
        LocalDateTime createdAt,
        long likeCount,
        boolean likedByMe,
        // 로그인한 멘티가 이 답변(멘토)에 이미 리뷰(별점)를 남겼는지
        boolean reviewedByMe,
        // 대댓글(답글) 목록. QuestionService에서 평평한 목록을 트리로 엮어 채운다.
        List<AnswerResponse> replies
) {

    // 좋아요/리뷰 정보 없이 (목록 등)
    public static AnswerResponse from(AnswerEntity answer) {
        return from(answer, 0L, false, false);
    }

    public static AnswerResponse from(AnswerEntity answer, long likeCount, boolean likedByMe, boolean reviewedByMe) {
        return new AnswerResponse(
                answer.getIdx(),
                answer.getContent(),
                answer.getWriter().getIdx(),
                answer.getWriter().getName(),
                answer.getParent() != null ? answer.getParent().getIdx() : null,
                answer.getCreatedAt(),
                likeCount,
                likedByMe,
                reviewedByMe,
                new ArrayList<>()
        );
    }

    // 평평한 목록(작성일 오름차순)을 parentAnswerIdx 기준 트리로 엮어 최상위 답변만 반환
    public static List<AnswerResponse> toTree(List<AnswerResponse> flatAnswers) {
        Map<Long, AnswerResponse> byIdx = new HashMap<>();
        for (AnswerResponse answer : flatAnswers) {
            byIdx.put(answer.idx(), answer);
        }

        List<AnswerResponse> roots = new ArrayList<>();
        for (AnswerResponse answer : flatAnswers) {
            if (answer.parentAnswerIdx() == null) {
                roots.add(answer);
                continue;
            }
            AnswerResponse parent = byIdx.get(answer.parentAnswerIdx());
            if (parent != null) {
                parent.replies().add(answer);
            } else {
                // 부모가 삭제된 경우 등 - 최상위로 취급
                roots.add(answer);
            }
        }
        return roots;
    }
}
