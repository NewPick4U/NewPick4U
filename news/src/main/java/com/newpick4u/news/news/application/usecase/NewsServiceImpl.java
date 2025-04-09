package com.newpick4u.news.news.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.news.news.application.dto.NewsInfoDto;
import com.newpick4u.news.news.application.dto.NewsTagDto;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.entity.NewsTag;
import com.newpick4u.news.news.domain.entity.TagInbox;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import com.newpick4u.news.news.domain.repository.TagInboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    private final TagInboxRepository tagInboxRepository;
    private final ObjectMapper objectMapper;
    // 테스트용 실패 상태 캐시
    private final Set<String> failedOnce = ConcurrentHashMap.newKeySet();

    @Transactional
      public void saveNewsInfo(NewsInfoDto dto) {
        simulateFailures(dto.aiNewsId()); // 테스트 조건 시뮬레이션

        if (newsRepository.existsByAiNewsId(dto.aiNewsId())) {
            throw new IllegalStateException("이미 저장된 뉴스입니다: " + dto.aiNewsId());
        }

        News news = News.create(dto.aiNewsId(), dto.title(), dto.content(), 0L);
        newsRepository.save(news);
      }

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
        if (dto.tagList() == null || dto.tagList().size() > 10) {
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
            log.warn("태그 직렬화 실패: {}", dto, e);
            throw new RuntimeException("태그 인박스 직렬화 실패", e);
        }
    }

    // 테스트용 시뮬레이션
    private void simulateFailures(String aiNewsId) {
        if ("fail-me".equals(aiNewsId)) {
            throw new RuntimeException("무조건 실패 유도");
        }
        if ("fail-once".equals(aiNewsId) && failedOnce.add(aiNewsId)) {
            throw new RuntimeException("첫 번째 실패 유도");
        }
    }
}
