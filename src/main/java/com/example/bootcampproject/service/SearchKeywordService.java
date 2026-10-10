package com.example.bootcampproject.service;

import com.example.bootcampproject.entity.SearchKeywordEntity;
import com.example.bootcampproject.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 검색어 인기도 집계 - 질문 검색이 실제로 실행될 때마다 카운트 누적. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchKeywordService {

    private static final int MAX_KEYWORD_LENGTH = 50;

    private final SearchKeywordRepository searchKeywordRepository;

    // 검색 실행 기록. 빈 키워드는 무시
    @Transactional
    public void record(String rawKeyword) {
        if (!StringUtils.hasText(rawKeyword)) {
            return;
        }
        String keyword = rawKeyword.strip();
        if (keyword.length() > MAX_KEYWORD_LENGTH) {
            keyword = keyword.substring(0, MAX_KEYWORD_LENGTH);
        }

        try {
            SearchKeywordEntity entity = searchKeywordRepository.findByKeyword(keyword).orElse(null);
            if (entity != null) {
                entity.increment();
            } else {
                searchKeywordRepository.save(new SearchKeywordEntity(keyword));
            }
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 unique 제약(uk_search_keyword)에 걸린 경우 - 카운트 1회 누락 정도는 무시
        }
    }

    // 검색화면 "추천 키워드" - 많이 검색된 순 최대 8개
    public List<String> getTopKeywords() {
        return searchKeywordRepository.findTop8ByOrderBySearchCountDescLastSearchedAtDesc().stream()
                .map(SearchKeywordEntity::getKeyword)
                .toList();
    }
}
