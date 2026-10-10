package com.example.bootcampproject.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SearchKeywordServiceTest {

    @Autowired
    SearchKeywordService searchKeywordService;

    @Test
    void record_incrementsExistingKeywordCount() {
        String keyword = "장학금-" + System.nanoTime();

        searchKeywordService.record(keyword);
        searchKeywordService.record(keyword);
        searchKeywordService.record(keyword);

        List<String> top = searchKeywordService.getTopKeywords();
        assertTrue(top.contains(keyword));
    }

    @Test
    void record_ignoresBlankKeyword() {
        // 예외 없이 조용히 무시되는지만 확인
        assertDoesNotThrow(() -> searchKeywordService.record(null));
        assertDoesNotThrow(() -> searchKeywordService.record("   "));
        assertDoesNotThrow(() -> searchKeywordService.record(""));
    }

    @Test
    void getTopKeywords_ordersByCountDescending() {
        String popular = "동아리-" + System.nanoTime();
        String rare = "자취-" + System.nanoTime();

        searchKeywordService.record(popular);
        searchKeywordService.record(popular);
        searchKeywordService.record(popular);
        searchKeywordService.record(rare);

        List<String> top = searchKeywordService.getTopKeywords();
        assertTrue(top.indexOf(popular) < top.indexOf(rare));
    }
}
