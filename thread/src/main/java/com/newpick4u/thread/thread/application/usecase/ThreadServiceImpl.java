package com.newpick4u.thread.thread.application.usecase;

import com.newpick4u.common.response.ApiResponse;
import com.newpick4u.thread.thread.application.dto.ThreadResponseDto;
import com.newpick4u.thread.thread.application.exception.ThreadException.NotFoundException;
import com.newpick4u.thread.thread.domain.entity.Thread;
import com.newpick4u.thread.thread.domain.entity.ThreadStatus;
import com.newpick4u.thread.thread.domain.repository.ThreadRepository;
import com.newpick4u.thread.thread.infrastructure.client.dto.CommentResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThreadServiceImpl implements ThreadService {

  private static final String OPEN_THREAD_KEY = "thread:open:id";
  private static final String HOT_TAG_KEY = "tag_count";
  private static final int MAX_THREADS = 5;
  private static final int MIN_HOT_TAG_COUNT = 30;
  private final ThreadRepository threadRepository;
  private final CommentClient commentClient;
  private final AiClient aiClient;
  private final RedisTemplate<String, String> redisTemplate;

  private static int getThreadsToCreate(List<Thread> openThreads) {
    return MAX_THREADS - openThreads.size();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ThreadResponseDto> getThreads(Pageable pageable) {
    Page<Thread> threadList = threadRepository.findAll(pageable);

    return threadList.map(ThreadResponseDto::from);
  }

  @Override
  @Transactional(readOnly = true)
  public Thread getThreadDetail(UUID threadId) {
    return threadRepository.findById(threadId).orElseThrow(NotFoundException::new);
  }

  @Override
  public Boolean existThread(UUID threadId) {
    return threadRepository.findById(threadId).isPresent();
  }

  /**
   * 댓글 도메인에서 쓰레드 댓글들 가져와서 ai 에게 여론분석 해달라
   */
  @Scheduled(fixedDelay = 5 * 60 * 1000)
  @Transactional
  public void createSummaryFromAi() {
    log.info("createSummaryFromAi Scheduler Start");
    Set<String> openThreadIds = getOpenThreadIdsFromRedis();

    // 핫 태그 없을 경우 넘어감
    if (openThreadIds.isEmpty()) {
      return;
    }

    for (String threadId : openThreadIds) {
      try {
        // 쓰레드에 달린 댓글들 가져오기
        ResponseEntity<ApiResponse<CommentResponse>> responseEntity = commentClient.getAllByThreadId(
            UUID.fromString(threadId));

        CommentResponse comments = Objects.requireNonNull(responseEntity.getBody(),
            "댓글 응답의 body가 null입니다.").data();

        if (CollectionUtils.isEmpty(comments.commentList())) {
          log.info("분석할 댓글 없음 [threadId={}]", threadId);
          continue;
        }

        // ai에게 요약
        String summary = aiClient.analyzeSummary(UUID.fromString(threadId), comments.commentList());

        // 요약 쓰레드에 저장
        Thread thread = threadRepository.findById(UUID.fromString(threadId))
            .orElseThrow(() -> new IllegalArgumentException("해당 쓰레드가 없습니다."));
        thread.addSummary(summary);

      } catch (Exception e) {
        log.error("여론 분석 실패 [threadId={}]: {}", threadId, e.getMessage(), e);
      }
    }

    log.info("createSummaryFromAi Scheduler End");
  }

  private Set<String> getOpenThreadIdsFromRedis() {
    Set<String> ids = redisTemplate.opsForSet().members(OPEN_THREAD_KEY);
    return ids != null ? ids : Collections.emptySet();
  }

  /**
   * 레디스로 핫태그 받아서 쓰레드 생성
   */
  @Scheduled(fixedDelay = 5 * 60 * 1000)
  @Transactional
  public void saveThread() {
    log.info("save thread Scheduler start");
    Set<String> hotTags = getHotTagsFromRedis();
    if (hotTags.isEmpty()) {
      return;
    }

    List<Thread> openThreads = threadRepository.findAllByStatus(ThreadStatus.OPEN);
    Map<String, Thread> tagToThreadMap = mapTagsToThreads(openThreads);

    // 존재하는 태그 업데이트
    Set<String> processedTags = updateExistingThreads(hotTags, tagToThreadMap);

    int threadsToCreate = getThreadsToCreate(openThreads);

    if (threadsToCreate > 0) {
      createAdditionalThreads(hotTags, threadsToCreate, tagToThreadMap);
    } else if (processedTags.isEmpty()) {
      replaceLowestScoreThread(hotTags, tagToThreadMap, openThreads);
    }

    if (!openThreads.isEmpty()) {
      cacheOpenThreadsToRedis(openThreads);
    }
    log.info("save thread Scheduler end");
  }

  private void cacheOpenThreadsToRedis(List<Thread> openThreads) {

    // Redis 초기화 후 저장
    redisTemplate.delete(OPEN_THREAD_KEY);

    redisTemplate.opsForSet().add(OPEN_THREAD_KEY, openThreads.stream()
        .limit(MAX_THREADS) // 최대 5개만 저장
        .map(t -> t.getId().toString()).distinct().toArray(String[]::new));
  }

  /**
   * 레디스에서 핫태그(score ≥ 30) 조회
   */
  private Set<String> getHotTagsFromRedis() {
    Set<ZSetOperations.TypedTuple<String>> sortedTags = redisTemplate.opsForZSet()
        .reverseRangeByScoreWithScores(HOT_TAG_KEY, MIN_HOT_TAG_COUNT, Double.MAX_VALUE);

    if (CollectionUtils.isEmpty(sortedTags)) {
      return Collections.emptySet();
    }

    // 태그 이름만 추출 (score 높은 순)
    return sortedTags.stream()
        .map(ZSetOperations.TypedTuple::getValue)
        .filter(Objects::nonNull)
        .map(value -> value.replace("tag_count", ""))
        .collect(Collectors.toCollection(LinkedHashSet::new)); // 순서 유지
  }

  /**
   * Map<String,Thread>
   */
  private Map<String, Thread> mapTagsToThreads(List<Thread> threads) {
    return threads.stream()
        .collect(Collectors.toMap(Thread::getTagName, Function.identity()));
  }

  // 이미 존재하는 쓰레드가 핫태그에 해당하는 경우 score 증가
  private Set<String> updateExistingThreads(Set<String> hotTags,
      Map<String, Thread> tagToThreadMap) {
    Set<String> processedTags = new HashSet<>();
    for (String tag : hotTags) {
      if (tagToThreadMap.containsKey(tag)) {
        Thread thread = tagToThreadMap.get(tag);
        thread.plusScore();
        threadRepository.save(thread);
        processedTags.add(tag);
      }
    }
    return processedTags;
  }

  // 부족한 쓰레드 개수만큼 새로 생성
  private void createAdditionalThreads(Set<String> hotTags,
      int threadsToCreate,
      Map<String, Thread> tagToThreadMap
  ) {
    int created = 0;
    for (String tag : hotTags) {
      if (tagToThreadMap.containsKey(tag)) {
        continue; // 이미 쓰레드가 존재하는 태그는 건너뜀
      }

      Thread newThread = Thread.create(tag);
      threadRepository.save(newThread);
      created++;

      if (created >= threadsToCreate) {
        break; // 필요한 개수만큼 생성 완료
      }
    }
  }

  // 기존 쓰레드가 5개이지만 핫태그에 해당하지 않는 경우 → score 낮은 쓰레드 교체
  private void replaceLowestScoreThread(Set<String> hotTags,
      Map<String, Thread> tagToThreadMap,
      List<Thread> openThreads) {

    Optional<Thread> lowestThreadOpt = openThreads.stream()
        .min(Comparator.comparingLong(Thread::getScore));

    if (lowestThreadOpt.isEmpty()) {
      return;
    }

    // 가장 점수가 낮은 쓰레드 종료
    Thread lowestThread = lowestThreadOpt.get();
    lowestThread.closedThread();
    threadRepository.save(lowestThread);
    openThreads.remove(lowestThread);

    hotTags.stream()
        .filter(tag -> !tagToThreadMap.containsKey(tag))
        .findFirst()
        .ifPresent(tag -> {
          Thread newThread = Thread.create(tag);
          threadRepository.save(newThread);

          openThreads.add(newThread);
          tagToThreadMap.put(tag, newThread);
        });
  }
}
