package com.newpick4u.news.news.infrastructure.jpa;

import com.newpick4u.news.news.application.dto.NewsSearchCriteria;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.model.Pagination;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import com.newpick4u.news.news.domain.repository.NewsRepositoryCustom;
import com.newpick4u.news.news.domain.repository.projection.NewsCreatedInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NewsRepositoryimpl implements NewsRepository {

    private final NewsJpaRepository newsJpaRepository;
    private final NewsRepositoryCustom newsRepositoryCustom;

    @Override
    public News save(News news) {
        return newsJpaRepository.save(news);
    }

    @Override
    public Optional<News> findByAiNewsId(String aiNewsId) {
        return newsJpaRepository.findByAiNewsId(aiNewsId);
    }

    @Override
    public boolean existsByAiNewsId(String aiNewsId) {
        return newsJpaRepository.existsByAiNewsId(aiNewsId);
    }

    @Override
    public Pagination<News> searchNewsList(NewsSearchCriteria criteria, boolean isMaster) {
        return newsRepositoryCustom.searchNewsList(criteria, isMaster);
    }

    @Override
    public Optional<News> findNewsByRole(UUID id, boolean isMaster) {
        return newsRepositoryCustom.findNewsByRole(id, isMaster);
    }

    @Override
    public Optional<News> findWithTagsByAiNewsId(String aiNewsId) {
        return newsRepositoryCustom.findWithTagsByAiNewsId(aiNewsId);
    }

    @Override
    public void flush() {
        newsJpaRepository.flush();
    }

    @Override
    public List<UUID> findAllActiveNewsIds(){
        return newsRepositoryCustom.findAllActiveNewsIds();
    }

    @Override
    public List<News> findLatestNews(int limit) {
        return newsRepositoryCustom.findLatestNews(limit);
    }

    @Override
    public List<News> findByIds(List<UUID> ids)  {
        return newsRepositoryCustom.findByIds(ids);
    }

    @Override
    public List<News> saveAll(List<News> newsList) {
        return newsJpaRepository.saveAll(newsList);
    }

    @Override
    public void deleteAll() {
        newsJpaRepository.deleteAll();
    }

    @Override
    public Optional<News> findById(UUID id) {
        return newsJpaRepository.findById(id);
    }

    @Override
    public List<News> findAll() {
        return newsJpaRepository.findAll();
    }

    @Override
    public void incrementViewCount(UUID newsId, long count){
        newsJpaRepository.incrementViewCount(newsId, count);
    }
    @Override
    public List<NewsCreatedInfo> findAllActiveNewsCreatedInfos() {
        return newsJpaRepository.findAllActiveNewsCreatedInfos();
    }
}
