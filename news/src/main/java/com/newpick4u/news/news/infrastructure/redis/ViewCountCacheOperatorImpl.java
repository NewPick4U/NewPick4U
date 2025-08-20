package com.newpick4u.news.news.infrastructure.redis;

import com.newpick4u.news.news.application.usecase.ViewCountCacheOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ViewCountCacheOperatorImpl implements ViewCountCacheOperator {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String USER_VIEW_SET_KEY = "view:user:%s";
    private static final String VIEW_COUNT_KEY = "view";
    private static final String POPULARITY_ZSET_KEY = "popular";

    @Override
    public boolean isViewToday(UUID newsId, Long userId) {
        String key = String.format(USER_VIEW_SET_KEY, userId);
        String newsIdStr = newsId.toString();

        Boolean alreadyViewed = redisTemplate.opsForSet().isMember(key, newsIdStr);
        if (Boolean.TRUE.equals(alreadyViewed)) {
            return false;
        }

        redisTemplate.opsForSet().add(key, newsIdStr);
        redisTemplate.expire(key, Duration.ofDays(1));
        return true;
    }

    @Override
    public void incrementViewCount(UUID newsId) {
        redisTemplate.opsForZSet().incrementScore(VIEW_COUNT_KEY, newsId.toString(), 1);
    }

    @Override
    public long getViewCount(UUID newsId) {
        Double score = redisTemplate.opsForZSet().score(VIEW_COUNT_KEY, newsId.toString());
        return score == null ? 0 : score.longValue();
    }

    @Override
    public void clearDailyViewKey(UUID newsId, Long userId) {
        redisTemplate.opsForSet().remove(String.format(USER_VIEW_SET_KEY, userId), newsId.toString());
    }

    @Override
    public void updatePopularityScore(UUID newsId, long viewCount, LocalDateTime createdAt) {
        long days = Duration.between(createdAt, LocalDateTime.now()).toDays();
        double score = viewCount / (1.0 + days);
        redisTemplate.opsForZSet().add(POPULARITY_ZSET_KEY, newsId.toString(), score);
        redisTemplate.opsForZSet().removeRange(POPULARITY_ZSET_KEY, 20, -1);
    }

    @Override
    public Set<String> getTopPopularNewsIds(int limit) {
        return redisTemplate.opsForZSet().reverseRange(POPULARITY_ZSET_KEY, 0, limit - 1);
    }
}