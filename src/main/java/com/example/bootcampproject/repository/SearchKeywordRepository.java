package com.example.bootcampproject.repository;

import com.example.bootcampproject.entity.SearchKeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeywordEntity, Long> {

    Optional<SearchKeywordEntity> findByKeyword(String keyword);

    List<SearchKeywordEntity> findTop8ByOrderBySearchCountDescLastSearchedAtDesc();
}
