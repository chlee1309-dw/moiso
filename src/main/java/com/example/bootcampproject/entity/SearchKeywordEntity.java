package com.example.bootcampproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** 검색어별 누적 검색 횟수 (검색화면 "추천 키워드" 집계용). */
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "search_keywords",
        uniqueConstraints = @UniqueConstraint(name = "uk_search_keyword", columnNames = "keyword"))
public class SearchKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_keyword_idx")
    private Long idx;

    @Column(name = "keyword", nullable = false, length = 50)
    private String keyword;

    @Column(name = "search_count", nullable = false)
    private long searchCount;

    @CreatedDate
    @Column(name = "first_searched_at", nullable = false, updatable = false)
    private LocalDateTime firstSearchedAt;

    @Column(name = "last_searched_at", nullable = false)
    private LocalDateTime lastSearchedAt;

    public SearchKeywordEntity(String keyword) {
        this.keyword = keyword;
        this.searchCount = 1;
        this.lastSearchedAt = LocalDateTime.now();
    }

    public void increment() {
        this.searchCount++;
        this.lastSearchedAt = LocalDateTime.now();
    }
}
