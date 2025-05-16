package com.newpick4u.news.news.application.scheduler;

import com.newpick4u.common.exception.CustomException;
import com.newpick4u.news.global.exception.NewsErrorCode;
import com.newpick4u.news.news.application.usecase.NewsRecommender;
import com.newpick4u.news.news.application.usecase.RecommendationCacheOperator;
import com.newpick4u.news.news.application.usecase.VectorConverter;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendScheduler {

    private final NewsRepository newsRepository;
    private final RecommendationCacheOperator recommendationCacheOperator;
    private final VectorConverter vectorConverterProvider;
    private final NewsRecommender newsRecommender;
    private final ExecutorService recommendExecutor;

    @Scheduled(cron = "0 0 3 * * *")
    public void updateAllUserRecommendations() {
        log.info("[배치 시작] 사용자 추천 뉴스 계산");

        List<Long> userIds = new ArrayList<>(recommendationCacheOperator.getCachedUserIds());
        List<UUID> candidateIds = newsRepository.findAllActiveNewsIds();

        List<CompletableFuture<Boolean>> tasks = userIds.stream()
                .map(userId -> CompletableFuture
                        .supplyAsync(() -> updateSingleUserRecommendation(userId, candidateIds), recommendExecutor)
                        .exceptionally(ex -> {
                            log.error("[추천 실패] userId={}, 에러={}", userId, ex.getMessage(), ex);
                            return false;
                        })
                )
                .toList();

        // 모든 작업 완료 대기
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        // 성공 개수 집계
        long successCount = tasks.stream()
                .map(CompletableFuture::join)
                .filter(Boolean::booleanValue)
                .count();

        log.info("[배치 완료] 총 사용자 수: {}, 성공 수: {}", userIds.size(), successCount);
    }

    private boolean updateSingleUserRecommendation(Long userId, List<UUID> candidateIds) {
        try {
            // 사용자 태그 로그 점수표 가져오기
            Map<String, Double> userTagMap = recommendationCacheOperator.getUserTagScore(userId);
            if (userTagMap.isEmpty()) return false;

            // 전역 태그 인덱스는 Redis에서 가져오기
            List<String> tagIndexList = recommendationCacheOperator.getGlobalTagIndexList();
            if (tagIndexList.isEmpty()) return false;

            // 1. 벡터화 (only 전역태그 인덱스, 사용자 태그로그 / 뉴스 벡터화는 스케줄러가 진행)
            double[] userVector = vectorConverterProvider.toUserVector(userTagMap, tagIndexList);

            // 2. 유사도 계산
            List<UUID> recommended = newsRecommender.recommendByContentVector(userVector, candidateIds);

            if (recommended.isEmpty()) {
                log.info("[추천 없음] userId={}", userId);
                return true;
            }

            // 3. Redis에 캐싱
            List<String> recommendedIds = recommended.stream()
                    .map(UUID::toString)
                    .toList();
            recommendationCacheOperator.storeRecommendedNews(userId, recommendedIds);
            log.info("[추천 저장 완료] userId={}, count={}", userId, recommendedIds.size());
            return true;
        } catch (Exception e) {
            log.error("[추천 저장 실패] userId={}", userId, e);
            throw CustomException.from(NewsErrorCode.NEWS_RECOMMENDATION_FAIL);
        }
    }
}
