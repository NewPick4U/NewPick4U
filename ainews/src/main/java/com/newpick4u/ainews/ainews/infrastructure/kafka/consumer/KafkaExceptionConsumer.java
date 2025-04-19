package com.newpick4u.ainews.ainews.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.newpick4u.ainews.ainews.application.EventPublisher;
import com.newpick4u.ainews.ainews.application.EventType;
import com.newpick4u.ainews.ainews.application.usecase.AiExceptionEventHandleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaExceptionConsumer {

  private final AiExceptionEventHandleService aiExceptionService;
  private final List<EventPublisher> eventPublishers;

  /**
   * 원본 뉴스 재처리 수신
   */
  @KafkaListener(
      topics = "${app.kafka.consumer.exception.topic.originnews-info-dlq.topic-name}",
      groupId = "${app.kafka.consumer.exception.topic.originnews-info-dlq.group-id}",
      containerFactory = "exceptionKafkaListenerContainerFactory"
  )
  public void listenOriginNewsDLQ(ConsumerRecord<String, String> record, Acknowledgment ack
  ) {
    String message = null;
    try {
      message = record.value();
      aiExceptionService.processAiNews(message);
      ack.acknowledge();

    } catch (Exception e) {
      log.error("OriginNewsDLQ Consume Fail : -> Retry {}", message, e);
    }
  }

  /**
   * AI 뉴스 저장 실패 재처리 수신
   */
  @KafkaListener(
      topics = "${app.kafka.consumer.exception.topic.ainews-dlq.topic-name}",
      groupId = "${app.kafka.consumer.exception.topic.ainews-dlq.group-id}",
      containerFactory = "exceptionKafkaListenerContainerFactory"
  )
  public void listenSaveFailDLQ(ConsumerRecord<String, String> record, Acknowledgment ack) {
    String message = null;
    try {
      message = record.value();
      aiExceptionService.saveAndSendTaskByListener(message);
      ack.acknowledge();

    } catch (JsonProcessingException e) {
      log.error("SaveFailDLQ Consume Fail : JSON Fail -> Drop : {}", message, e);
      ack.acknowledge(); // JSON 관련 예외는 재처리 안함

    } catch (Exception e) {
      log.error("SaveFailDLQ Consume Fail : -> Retry {}", message, e);
    }
  }

  /**
   * 뉴스 전송 실패 재처리 수신
   */
  @KafkaListener(
      topics = "${app.kafka.consumer.exception.topic.news-info-dlq.topic-name}",
      groupId = "${app.kafka.consumer.exception.topic.news-info-dlq.group-id}",
      containerFactory = "exceptionKafkaListenerContainerFactory"
  )
  public void listenSendNewsFailDLQ(ConsumerRecord<String, String> record, Acknowledgment ack) {
    String message = null;
    try {
      message = record.value();
      sendMessage(message, EventType.NEWS_INFO_SEND);
      ack.acknowledge();

    } catch (Exception e) {
      log.error("SaveFailDLQ Consume Fail : -> Retry {}", message, e);
    }
  }

  /**
   * 태그 전송 실패 재처리 수신
   */
  @KafkaListener(
      topics = "${app.kafka.consumer.exception.topic.tag-info-dlq.topic-name}",
      groupId = "${app.kafka.consumer.exception.topic.tag-info-dlq.group-id}",
      containerFactory = "exceptionKafkaListenerContainerFactory"
  )
  public void listenSendTagFailDLQ(ConsumerRecord<String, String> record, Acknowledgment ack) {
    String message = null;
    try {
      message = record.value();
      sendMessage(message, EventType.TAG_INFO_SEND);
      ack.acknowledge();

    } catch (Exception e) {
      log.error("SaveFailDLQ Consume Fail : -> Retry {}", message, e);
    }
  }

  private void sendMessage(String message, EventType eventType) {
    this.eventPublishers.stream()
        .filter(ep -> ep.isSupport(eventType))
        .findAny()
        .orElseThrow(() -> new RuntimeException("Not Support EventType : " + eventType.name()))
        .sendMessage(message, eventType);
  }
}
