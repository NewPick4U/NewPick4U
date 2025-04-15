package com.newpick4u.news.news.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.common.resolver.dto.CurrentUserInfoDto;
import com.newpick4u.common.resolver.dto.UserRole;
import com.newpick4u.news.news.application.dto.NewsInfoDto;
import com.newpick4u.news.news.application.dto.NewsTagDto;
import com.newpick4u.news.news.application.dto.response.NewsResponseDto;
import com.newpick4u.news.news.application.dto.response.NewsSummaryDto;
import com.newpick4u.news.news.application.dto.response.PageResponse;
import com.newpick4u.news.news.domain.critria.NewsSearchCriteria;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.entity.NewsTag;
import com.newpick4u.news.news.domain.entity.TagInbox;
import com.newpick4u.news.news.domain.model.Pagination;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import com.newpick4u.news.news.domain.repository.TagInboxRepository;
import com.newpick4u.news.news.infrastructure.redis.UserTagRedisOperator;
import com.newpick4u.news.news.infrastructure.util.NewsRecommender;
import com.newpick4u.news.news.infrastructure.util.TagVectorConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    private final TagInboxRepository tagInboxRepository;
    private final ObjectMapper objectMapper;
    private final UserTagRedisOperator userTagRedisOperator;

    @Override
    @Transactional
      public void saveNewsInfo(NewsInfoDto dto) {
          if (newsRepository.existsByAiNewsId(dto.aiNewsId())) {
              throw new IllegalStateException("이미 저장된 뉴스입니다: " + dto.aiNewsId());
          }
          News news = News.create(dto.aiNewsId(), dto.title(), dto.content(), dto.url(), dto.publishedDate(), 0L);
          newsRepository.save(news);
      }

    @Override
    @Transactional
    public void updateNewsTagList(NewsTagDto dto) {
        validateTagListSize(dto);
        newsRepository.findByAiNewsId(dto.aiNewsId())
                .ifPresentOrElse(
                        news -> applyTagList(news, dto),
                        () -> saveInbox(dto)
                );
    }

    // 내부 메서드
    private void validateTagListSize(NewsTagDto dto) {
        if (dto.tagList() == null || dto.tagList().size() > 11) {
            throw new IllegalArgumentException("뉴스 태그는 최대 10개까지 존재합니다.");
        }
    }

    private void applyTagList(News news, NewsTagDto dto) {
        List<NewsTag> tags = dto.tagList().stream()
                .map(tag -> NewsTag.create(tag.id(), tag.name(), news))
                .toList();
        news.addTags(tags);
    }

    private void saveInbox(NewsTagDto dto) {
        String json = serializeToJson(dto);
        TagInbox inbox = TagInbox.create(dto.aiNewsId(), json);
        tagInboxRepository.save(inbox);
    }

    private String serializeToJson(NewsTagDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("태그 인박스 직렬화 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NewsResponseDto getNews(UUID id, CurrentUserInfoDto userInfoDto) {
        boolean isMaster = userInfoDto.role() == UserRole.ROLE_MASTER;
        News news = newsRepository.findNewsByRole(id, isMaster)
                .orElseThrow(() -> new IllegalArgumentException("뉴스를 찾을 수 없습니다."));

        List<String> tags = news.getNewsTagList().stream()
                .map(NewsTag::getName)
                .toList();

        userTagRedisOperator.incrementUserTags(userInfoDto.userId(), tags);

        return NewsResponseDto.from(news);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsSummaryDto> searchNewsList(NewsSearchCriteria request, CurrentUserInfoDto userInfoDto) {
        boolean isMaster = userInfoDto.role() == UserRole.ROLE_MASTER;
        Pagination<News> pagination = newsRepository.searchNewsList(request, isMaster);
        return PageResponse.from(pagination).map(NewsSummaryDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsSummaryDto> recommendTop10(CurrentUserInfoDto userInfo) {
        Long userId = userInfo.userId();

        // 1. Redis 캐시 먼저 조회
        List<String> cachedNewsIds = userTagRedisOperator.getCachedRecommendedNews(userId);
        if (cachedNewsIds != null && !cachedNewsIds.isEmpty()) {
            List<UUID> ids = cachedNewsIds.stream().map(UUID::fromString).toList();
            List<News> newsList = newsRepository.findByIds(ids);
            return newsList.stream().map(NewsSummaryDto::from).toList();
        }

        // 2. 캐시 없으면 최신 뉴스 fallback
        List<News> fallbackNews = newsRepository.findLatestNews(10);
        return fallbackNews.stream()
                .map(NewsSummaryDto::from)
                .toList();
    }
}
