package com.newpick4u.news.news.domain.repository;

import com.newpick4u.news.news.domain.critria.NewsSearchCriteria;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.model.Pagination;

import java.util.Optional;
import java.util.UUID;

public interface NewsRepository {
    News save(News news);
    Optional<News> findByAiNewsId(String aiNewsId);
    Optional<News> findById(UUID id);
    boolean existsByAiNewsId(String aiNewsId);
    Pagination<News> searchNewsList(NewsSearchCriteria request, boolean isMaster);
    Optional<News> findDetail(UUID id);
    Optional<News> findActiveDetail(UUID id);

}