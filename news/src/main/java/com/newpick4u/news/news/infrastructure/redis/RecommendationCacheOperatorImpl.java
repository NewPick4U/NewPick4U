package com.newpick4u.news.news.infrastructure.redis;

import com.newpick4u.common.exception.CustomException;
import com.newpick4u.news.global.exception.NewsErrorCode;
import com.newpick4u.news.news.application.usecase.RecommendationCacheOperator;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecommendationCacheOperatorImpl implements RecommendationCacheOperator {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    private static final int MAX_TAGS = 50;

    private static final String TAG_KEY_PATTERN = "user:*:tags";
    private static final String USER_TAG_KEY_PREFIX = "user:";
    private static final String USER_TAG_KEY_SUFFIX = ":tags";
    private static final String RECOMMEND_KEY_SUFFIX = ":recommend";
    private static final String RECOMMEND_FALLBACK_KEY = "news:recommend:fallback";

    private static final Duration TAG_TTL = Duration.ofDays(30);
    private static final Duration RECOMMEND_CACHE_TTL = Duration.ofDays(1);
    private static final Duration RECOMMEND_FALLBACK_TTL = Duration.ofSeconds(30);

    // 태그 카운트 증가 (태그 기록 + 제한 개수 초과 시 삭제 + TTL 설정 (원자적 처리))
    @Override
    public void incrementUserTagScore(Long userId, List<String> tagNames) {
        String key = buildKey(userId);

        for (String tag : tagNames) {
            redisTemplate.opsForZSet().incrementScore(key, tag, 1);
        }

        redisTemplate.expire(key, TAG_TTL);
    }

    // 유저 태그 점수 맵 가져오기
    @Override
    public Map<String, Double> getUserTagScore(Long userId) {
        String key = buildKey(userId);
        Set<ZSetOperations.TypedTuple<String>> rawTags =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        if (rawTags == null) return Collections.emptyMap();

        Map<String, Double> tagScoreMap = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> tuple : rawTags) {
            if (tuple.getValue() != null && tuple.getScore() != null) {
                tagScoreMap.put(tuple.getValue(), tuple.getScore());
            }
        }
        return tagScoreMap;
    }

    // 추천 뉴스 캐시 저장
    @Override
    public void storeRecommendedNews(Long userId, List<String> newsIds) {
        String realKey = recommendKey(userId);
        String tempKey = realKey + ":tmp";

        // 1. 임시 키에 먼저 데이터 삽입
        redisTemplate.opsForList().rightPushAll(tempKey, newsIds);
        redisTemplate.expire(tempKey, RECOMMEND_CACHE_TTL);

        // 2. 임시 키 -> 실제 키로 원자적 전환
        redisTemplate.rename(tempKey, realKey);
    }

    // 추천 뉴스 캐시 조회
    @Override
    public List<String> getRecommendedNews(Long userId) {
        String key = recommendKey(userId);
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    // 추천 캐시용 사용자 ID 목록 조회 (SCAN 기반)
    @Override
    public Set<Long> getCachedUserIds() {
        Set<Long> userIds = new HashSet<>();
        ScanOptions scanOptions = ScanOptions.scanOptions().match(TAG_KEY_PATTERN).count(1000).build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(scanOptions)) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next()); // byte[] → String
                Long userId = extractUserIdFromKey(key);
                if (userId != null) {
                    userIds.add(userId);
                }
            }

        } catch (Exception e) {
            throw CustomException.from(NewsErrorCode.REDIS_SCAN_FAIL);
        }

        return userIds;
    }

    // 전역태그인덱스리스트 가져오기
    @Override
    public List<String> getGlobalTagIndexList() {
        return redisTemplate.opsForList().range("tag:index:list", 0, -1);
    }


    @Override
    public void cacheFallbackLatestNews(List<UUID> newsIds) {
        String value = newsIds.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
        redisTemplate.opsForValue().set(RECOMMEND_FALLBACK_KEY, value, RECOMMEND_FALLBACK_TTL);
    }

    @Override
    public List<UUID> getFallbackLatestNews() {
        String value = redisTemplate.opsForValue().get(RECOMMEND_FALLBACK_KEY);
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }
    // 내부메서드

    // 태그 개수 제한 초과 시 오래된 태그 제거
    private void trimTagLimit(String key) {
        Long size = redisTemplate.opsForZSet().zCard(key);
        if (size != null && size > MAX_TAGS) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - MAX_TAGS - 1);
        }
    }

    private String buildKey(Long userId) {
        return USER_TAG_KEY_PREFIX + userId + USER_TAG_KEY_SUFFIX;
    }

    // 추천 캐시 키
    private String recommendKey(Long userId) {
        return USER_TAG_KEY_PREFIX + userId + RECOMMEND_KEY_SUFFIX;
    }

    private Long extractUserIdFromKey(String key) {
        try {
            String[] parts = key.split(":" );
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            throw CustomException.from(NewsErrorCode.REDIS_SCAN_FAIL);
        }
    }

    @Override
    public void removeFromAllRecommendations(UUID newsId) {
        String newsIdStr = newsId.toString();

        // 1. 사용자 추천 캐시 제거
        Set<Long> userIds = getCachedUserIds();
        for (Long userId : userIds) {
            String key = "user:" + userId + ":recommend";
            List<String> list = redisTemplate.opsForList().range(key, 0, -1);
            if (list != null && list.contains(newsIdStr)) {
                List<String> filtered = list.stream().filter(id -> !id.equals(newsIdStr)).toList();
                String tmpKey = key + ":tmp";
                redisTemplate.delete(tmpKey);
                if (!filtered.isEmpty()) {
                    redisTemplate.opsForList().rightPushAll(tmpKey, filtered);
                    redisTemplate.rename(tmpKey, key);
                } else {
                    redisTemplate.delete(key);
                }
            }
        }

        // 2. fallback 추천 캐시 제거
        String fallback = redisTemplate.opsForValue().get("news:recommend:fallback");
        if (fallback != null && fallback.contains(newsIdStr)) {
            List<String> filtered = Arrays.stream(fallback.split(","))
                    .filter(id -> !id.equals(newsIdStr))
                    .toList();
            if (!filtered.isEmpty()) {
                redisTemplate.opsForValue().set("news:recommend:fallback", String.join(",", filtered));
            } else {
                redisTemplate.delete("news:recommend:fallback");
            }
        }

        // 3. 인기 ZSet 제거
        redisTemplate.opsForZSet().remove("popular", newsIdStr);
    }

}