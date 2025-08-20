package com.newpick4u.news.news.application.scheduler;

import com.newpick4u.news.news.application.usecase.ViewCountCacheOperator;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import com.newpick4u.news.news.domain.repository.projection.NewsCreatedInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final ViewCountCacheOperator viewCountCacheOperator;
    private final RedisTemplate<String, String> redisTemplate;
    private final NewsRepository newsRepository;
    private static final String VIEW_COUNT_KEY_PREFIX = "view";

    @Scheduled(fixedDelay = 3600000) // 1시간마다 실행
    public void syncViewCounts() {
        Set<ZSetOperations.TypedTuple<String>> entries = redisTemplate.opsForZSet().rangeWithScores(VIEW_COUNT_KEY_PREFIX, 0, -1);

        if (entries == null || entries.isEmpty()) return;

        entries.parallelStream().forEach(tuple -> {
            String newsIdStr = tuple.getValue();
            Double score = tuple.getScore();
            if (newsIdStr == null || score == null || score <= 0) return;

            UUID newsId = UUID.fromString(newsIdStr);
            long count = score.longValue();

            try {
                // DB 반영
                newsRepository.incrementViewCount(newsId, count);
                // Redis 차감 (decrement)
                redisTemplate.opsForZSet().incrementScore(VIEW_COUNT_KEY_PREFIX, newsIdStr, -count);
                log.info("[ViewSync] 동기화 완료 - newsId={}, +{}회 반영", newsId, count);
            } catch (Exception e) {
                log.error("[ViewSync] 동기화 실패 - newsId={}", newsId, e);
            }
        });
    }

    @Scheduled(cron = "0 * * * * *") // 1분마다
    public void updatePopularityScores() {
        List<NewsCreatedInfo> infoList = newsRepository.findAllActiveNewsCreatedInfos();

        for (NewsCreatedInfo info : infoList) {
            try {
                UUID newsId = info.getId();
                LocalDateTime createdAt = info.getCreatedAt();

                long viewCount = viewCountCacheOperator.getViewCount(newsId);
                long daysSince = Duration.between(createdAt, LocalDateTime.now()).toDays();
                double score = viewCount / (1.0 + daysSince);

                viewCountCacheOperator.updatePopularityScore(newsId, viewCount, createdAt);

                log.info("[인기점수 갱신] newsId={}, viewCount={}, days={}, score={}", newsId, viewCount, daysSince, score);
            } catch (Exception e) {
                log.error("[인기점수 갱신 실패] newsId={}", info.getId(), e);
            }
        }
    }
} 
