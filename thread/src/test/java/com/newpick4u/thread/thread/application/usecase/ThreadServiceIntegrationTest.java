package com.newpick4u.thread.thread.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.newpick4u.thread.thread.domain.entity.Thread;
import com.newpick4u.thread.thread.domain.entity.ThreadStatus;
import com.newpick4u.thread.thread.domain.repository.ThreadRepository;
import com.newpick4u.thread.thread.infrastructure.client.CommentClientImpl;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public class ThreadServiceIntegrationTest {

  @MockitoBean
  private CommentClientImpl commentClient;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private ThreadServiceImpl threadService;

  @Autowired
  private ThreadRepository threadRepository;

  @BeforeEach
  void setUp() {
    redisTemplate.getConnectionFactory().getConnection().flushAll(); // 초기화
    threadRepository.deleteAll();
  }

  @Test
  @DisplayName("실제 Redis에 데이터를 넣고 Thread를 생성한다")
  void integrationTestUsingRealRedis() {
    // given
    redisTemplate.opsForZSet().add("tag_count", "AI", 42);
    redisTemplate.opsForZSet().add("tag_count", "게임", 55);

    // when
    threadService.saveThread();

    // then
    Pageable pageable = PageRequest.of(0, 10);
    Page<Thread> savedThreads = threadRepository.findAll(pageable);
    assertThat(savedThreads).hasSize(2);
    assertThat(savedThreads).extracting("tagName").containsExactlyInAnyOrder("AI", "게임");
  }

  @Test
  @DisplayName("핫태그 중 이미 존재하는 태그가 있을 경우, 해당 쓰레드의 score만 증가")
  void integration_existingThread_shouldIncreaseScoreOnly() {
    // given
    Thread existing = Thread.create("AI");
    threadRepository.save(existing);

    redisTemplate.opsForZSet().add("tag_count", "AI", 42);

    // when
    threadService.saveThread();

    // then
    Pageable pageable = PageRequest.of(0, 10);
    Page<Thread> threads = threadRepository.findAll(pageable);
    assertThat(threads).hasSize(1);
    Thread thread = threads.getContent().get(0);
    assertThat(thread.getTagName()).isEqualTo("AI");
    assertThat(thread.getScore()).isEqualTo(2L);
  }

  @Test
  @DisplayName("open된 쓰레드가 5개 이상이고 핫태그에 해당하지 않는 경우, 가장 score 낮은 쓰레드를 종료 후 새 쓰레드 생성")
  void integration_replaceLowestScoreThread() {
    // given: 점수가 다른 open 상태의 쓰레드 5개 저장
    for (int i = 0; i < 5; i++) {
      Thread thread = Thread.create("oldTag" + i);
      for (int j = 0; j < i + 1; j++) {
        thread.plusScore(); // 점수 다르게 설정
      }
      threadRepository.save(thread);
    }

    redisTemplate.opsForZSet().add("tag_count", "newHotTag", 42);

    // when
    threadService.saveThread();

    // then
    Pageable pageable = PageRequest.of(0, 10);
    Page<Thread> allThreads = threadRepository.findAll(pageable);
    long openCount = allThreads.stream().filter(t -> t.getStatus() == ThreadStatus.OPEN).count();
    long closedCount = allThreads.stream().filter(t -> t.getStatus() == ThreadStatus.CLOSED)
        .count();

    assertThat(openCount).isEqualTo(5); // 총 5개 유지
    assertThat(closedCount).isEqualTo(1); // 하나는 닫힘
    assertThat(allThreads).extracting("tagName").contains("newHotTag"); // 새 태그 생성 확인
  }

  @Test
  @DisplayName("레디스에 저장된 오픈된 쓰레드 가져오기")
  void get_thread_from_cache() throws Exception {
    // given: 점수가 다른 open 상태의 쓰레드 5개 저장
    for (int i = 0; i < 5; i++) {
      Thread thread = Thread.create("oldTag" + i);
      for (int j = 0; j < i + 1; j++) {
        thread.plusScore(); // 점수 다르게 설정
      }
      threadRepository.save(thread);
    }

    redisTemplate.opsForZSet().add("tag_count", "newHotTag", 42);

    // when
    threadService.saveThread();

    //then

    Set<String> cachedIds = redisTemplate.opsForSet().members("thread:open:id");
    assertThat(cachedIds).isNotNull();

    boolean containsNewHotTag = cachedIds.stream()
        .map(UUID::fromString)
        .map(id -> threadRepository.findById(id))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .anyMatch(thread -> thread.getTagName().equals("newHotTag"));

    assertThat(containsNewHotTag).isTrue();
  }
}
