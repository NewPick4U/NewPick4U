package com.newpick4u.ainews.ainews.infrastructure.kafka.producer;

import com.newpick4u.ainews.ainews.application.EventPublisher;
import com.newpick4u.ainews.ainews.application.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaAiProducer implements EventPublisher {

  private final KafkaTemplate<String, String> exceptionKafkaTemplate;

  @Value("${app.kafka.producer.exceptional.topic.originnews-info-dlq.topic-name}")
  private String apiFailExceptionTopicName;

  @Value("${app.kafka.producer.exceptional.topic.ainews-dlq.topic-name}")
  private String saveFailExceptionTopicName;


  public KafkaAiProducer(
      @Qualifier("exceptionKafkaTemplate")
      KafkaTemplate<String, String> exceptionKafkaTemplate) {
    this.exceptionKafkaTemplate = exceptionKafkaTemplate;
  }

  @Override
  public boolean isSupport(EventType eventType) {
    if (eventType.equals(EventType.FAIL_PROCESS_AINEWS)) {
      return true;
    }
    if (eventType.equals(EventType.FAIL_SAVE_AINEWS)) {
      return true;
    }

    return false;
  }

  @Override
  public void sendMessage(String message, EventType eventType) {
    switch (eventType) {
      case FAIL_PROCESS_AINEWS -> {
        sendApiCallFailDLQ(message);
      }
      case FAIL_SAVE_AINEWS -> {
        saveDBFailDLQ(message);
      }
    }
  }

  // 실패 케이스 전송 : API 호출 실패 케이스
  private void sendApiCallFailDLQ(String message) {
    try {
      exceptionKafkaTemplate.send(apiFailExceptionTopicName, message)
          .thenAccept(result -> {
          }).exceptionally(ex -> {
            // DLQ 전송 실패는 로그만 남긴다.
            log.error("Fail Send DLQ Message [{}] : message={}", apiFailExceptionTopicName, message,
                ex);
            return null;
          });

    } catch (Exception e) {
      // DLQ 전송 실패는 로그만 남긴다.
      log.error("Fail Send DLQ Message [{}] : message={}", apiFailExceptionTopicName, message, e);
    }
  }

  // 실패 케이스 전송 : DB 저장 실패 케이스
  private void saveDBFailDLQ(String message) {
    try {
      exceptionKafkaTemplate.send(saveFailExceptionTopicName, message)
          .thenAccept(result -> {
          }).exceptionally(ex -> {
            // DLQ 전송 실패는 로그만 남긴다.
            log.error("Fail Send DLQ Message [{}] : message={}", saveFailExceptionTopicName, message,
                ex);
            return null;
          });

    } catch (Exception e) {
      // DLQ 전송 실패는 로그만 남긴다.
      log.error("Fail Send DLQ Message [{}] : message={}", saveFailExceptionTopicName, message, e);
    }
  }
}
