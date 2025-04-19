package com.newpick4u.ainews.ainews.infrastructure.kafka.consumer;

import com.newpick4u.ainews.ainews.application.usecase.AiNormalEventHandleService;
import com.newpick4u.ainews.global.common.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaNormalConsumer {

  private final AiNormalEventHandleService aiNormalEventHandleService;

  /**
   * 원본 뉴스 수신
   */
  @KafkaListener(
      topics = "${app.kafka.consumer.normal.topic.originnews-info.topic-name}",
      groupId = "${app.kafka.consumer.normal.topic.originnews-info.group-id}",
      containerFactory = "normalKafkaListenerContainerFactory"
  )
  public void listenOriginNews(ConsumerRecord<String, String> record, Acknowledgment ack) {
    log.info("[Message Consume Start] : {}", record.topic());
    long startTime = System.currentTimeMillis();
    String message = null;

    try {
      message = record.value();
      aiNormalEventHandleService.processAiNews(message);
    } catch (Exception e) {
      log.error("Consume Fail -> Retry : {}", record.value(), e);
    } finally {
      // 로직 처리 실패 시 직접 DLQ 에 전송하므로 정상큐는 항상 ACK 처리
      ack.acknowledge();
    }

    long endTime = System.currentTimeMillis();
    log.info("[Message Consumed] : {} : excution time : {}",
        record.topic(),
        CommonUtil.formatMillisToTime(endTime - startTime));
  }
}
