package com.newpick4u.news.news.infrastructure.redis;

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
public class UserTagRedisOperator {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    private static final int MAX_TAGS = 50;
    private static final Duration TAG_TTL = Duration.ofDays(30); // 30일간 미접속 시 태그 만료
    private static final Duration RECOMMEND_CACHE_TTL = Duration.ofDays(1);
    private static final String LOCK_PREFIX = "lock:user:tags:";
    private static final String TAG_KEY_PATTERN = "user:*:tags";

    // 태그 카운트 증가 (태그 기록 + 제한 개수 초과 시 삭제 + TTL 설정 (원자적 처리))
    public void incrementUserTags(Long userId, List<String> tagNames) {
        String key = buildKey(userId);
        String lockKey = LOCK_PREFIX + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(3, 2, TimeUnit.SECONDS)) { // timeout: 대기 3초, 점유 2초
                for (String tag : tagNames) {
                    redisTemplate.opsForZSet().incrementScore(key, tag, 1);
                }

                trimTagLimit(key);
                redisTemplate.expire(key, TAG_TTL);
            } else {
                throw new IllegalStateException("Redis 락 획득 실패: userId=" + userId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Redis 락 인터럽트", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 유저 태그 점수 맵 가져오기
    public Map<String, Double> getUserTagScoreMap(Long userId) {
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
    public void cacheRecommendedNews(Long userId, List<String> newsIds) {
        String key = recommendKey(userId);
        redisTemplate.delete(key); // 덮어쓰기
        redisTemplate.opsForList().rightPushAll(key, newsIds);
        redisTemplate.expire(key, RECOMMEND_CACHE_TTL);
    }

    // 추천 뉴스 캐시 조회
    public List<String> getCachedRecommendedNews(Long userId) {
        String key = recommendKey(userId);
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    // 추천 캐시용 사용자 ID 목록 조회 (SCAN 기반)
    public Set<Long> getAllUserIds() {
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
            // 로그 처리 또는 예외 변환 처리
            throw new RuntimeException("Redis SCAN 중 오류 발생", e);
        }

        return userIds;
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
        return "user:" + userId + ":tags";
    }

    // 추천 캐시 키
    private String recommendKey(Long userId) {
        return "user:" + userId + ":recommend";
    }

    private Long extractUserIdFromKey(String key) {
        try {
            String[] parts = key.split(":" );
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }
}