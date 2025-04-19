package com.newpick4u.newsorigin.newsorigin.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.newsorigin.newsorigin.application.GetOriginBodyClient;
import com.newpick4u.newsorigin.newsorigin.application.MessageClient;
import com.newpick4u.newsorigin.newsorigin.application.OriginCollectClient;
import com.newpick4u.newsorigin.newsorigin.application.dto.NewNewsOriginDto;
import com.newpick4u.newsorigin.newsorigin.application.dto.SendNewOriginDto;
import com.newpick4u.newsorigin.newsorigin.application.parser.BodyParser;
import com.newpick4u.newsorigin.newsorigin.domain.entity.NewsOrigin;
import com.newpick4u.newsorigin.newsorigin.domain.repository.NewsOriginRepository;
import io.micrometer.common.util.StringUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NewsOriginServiceImpl implements NewsOriginService {

  private static final int SEND_BATCH_SIZE = 20;
  private final OriginCollectClient originCollectClient;
  private final GetOriginBodyClient getOriginBodyClient;
  private final NewsOriginRepository newsOriginRepository;
  private final BodyParser bodyParser;
  private final MessageClient messageClient;
  private final ObjectMapper mapper = new ObjectMapper();

  // 뉴스 수집 및 저장
  @Override
  public int collectOriginNews() {
    int count = 0;
    try {
      ArrayList<NewNewsOriginDto> originNewsList = originCollectClient.getOriginNewsList();

      List<NewsOrigin> newsOriginList = originNewsList.stream()
          .map(dto -> NewsOrigin.create(dto.title(), dto.url(), dto.publishedDate()))
          .toList();

      // 중복 처리를 위한
      int saveCount = 0;
      for (NewsOrigin newsOrigin : newsOriginList) {

        try {
          newsOriginRepository.save(newsOrigin);
          saveCount++;
        } catch (DataIntegrityViolationException e) {
          log.warn("Already Saved News : url={} ", newsOrigin.getUrl());
        }
      }
      return saveCount;

    } catch (JsonProcessingException e) {
      log.error("", e);
    }
    return count;
  }

  // 뉴스 조회 및 전송
  @Override
  public int sendNewsOriginMessages() {

    // RDB 조회
    List<NewsOrigin> beforeSentNewsOrigin = newsOriginRepository.getAllByBeforeSentQueue();
    AtomicInteger updateCount = new AtomicInteger(0);
    if (beforeSentNewsOrigin.isEmpty()) {
      return updateCount.get();
    }

    ArrayDeque<NewsOrigin> beforeSentNewsOriginQueue = new ArrayDeque<>(beforeSentNewsOrigin);
    ExecutorService threadPool = Executors.newFixedThreadPool(SEND_BATCH_SIZE);

    // BATCH_SIZE 만큼 나눠서 작업 수행
    while (!beforeSentNewsOriginQueue.isEmpty()) {
      ArrayList<NewsOrigin> currentTarget = new ArrayList<>();
      while (!beforeSentNewsOriginQueue.isEmpty() && currentTarget.size() < SEND_BATCH_SIZE) {
        currentTarget.add(beforeSentNewsOriginQueue.pop());
      }
      sendTask(threadPool, currentTarget, updateCount);

      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        log.error("InterruptedException : sendNewsOriginMessages.sleep ", e);
      }
    }

    threadPool.shutdown();
    log.info("threadPool isShutdown : {}", threadPool.isShutdown());

    return updateCount.get();
  }

  /**
   * 지정된 List 만큼 원본획득 ~ 메세지 전송 작업을 수행: NewsOrigin 하나씩 원본작업획득, 메세지전송, 상태업데이트 수행
   *
   * @param threadPool           동시작업을 수행할 쓰레드풀
   * @param beforeSentNewsOrigin 전송 작업 전 NewsOrigin 목록
   * @param updateCount          업데이트 된 수를 카운트 하기 위한 변수
   */
  private void sendTask(
      ExecutorService threadPool,
      List<NewsOrigin> beforeSentNewsOrigin,
      AtomicInteger updateCount) {

    ArrayList<CompletableFuture<Void>> sendTaskList = new ArrayList<>();
    for (NewsOrigin newsOrigin : beforeSentNewsOrigin) {

      CompletableFuture<Void> sendTask = CompletableFuture.runAsync(() -> {
        // 뉴스 원본 기사 획득
        String originNewsBody = getOriginBodyClient.getOriginNewsBody(newsOrigin.getUrl());
        if (StringUtils.isBlank(originNewsBody)) {
          return;
        }

        // 기사 본문 추출
        String extractedMainBody = bodyParser.extractMainBody(originNewsBody,
            newsOrigin.getTitle());
        SendNewOriginDto sendNewOriginDto = SendNewOriginDto.of(
            newsOrigin.getId(),
            newsOrigin.getTitle(),
            newsOrigin.getUrl(),
            newsOrigin.getNewsPublishedDate(),
            extractedMainBody);

        if (StringUtils.isBlank(sendNewOriginDto.body())) {
          // 기사 본문 파싱 불가 케이스 : 기사 본문이 자바스크립트로 이루어진 케이스
          newsOrigin.sendFail();
          newsOriginRepository.save(newsOrigin); // update
          return;
        }

        String jsonMessage = null;
        try {
          jsonMessage = mapper.writeValueAsString(sendNewOriginDto);
        } catch (JsonProcessingException e) {
          log.error("", e);
          return;
        }

        // message send
        boolean isSuccess = messageClient.sendNewsOriginMessage(jsonMessage);
        if (isSuccess) {
          newsOrigin.sentToQueue();
          newsOriginRepository.save(newsOrigin); // update
          updateCount.incrementAndGet();
        }
      }, threadPool);

      sendTaskList.add(sendTask);
    }

    sendTaskList.forEach(CompletableFuture::join);
  }
}
