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
public class KafkaNewsProducer implements EventPublisher {

  private final KafkaTemplate<String, String> normalKafkaTemplate;
  private final KafkaTemplate<String, String> exceptionKafkaTemplate;

  @Value("${app.kafka.producer.normal.topic.news-info.topic-name}")
  private String newsTopicName;

  @Value("${app.kafka.producer.exceptional.topic.news-info-dlq.topic-name}")
  private String newsExceptionTopicName;


  public KafkaNewsProducer(
      @Qualifier("normalKafkaTemplate")
      KafkaTemplate<String, String> normalKafkaTemplate,
      @Qualifier("exceptionKafkaTemplate")
      KafkaTemplate<String, String> exceptionKafkaTemplate) {
    this.normalKafkaTemplate = normalKafkaTemplate;
    this.exceptionKafkaTemplate = exceptionKafkaTemplate;
  }

  @Override
  public boolean isSupport(EventType eventType) {
    if (eventType.equals(EventType.NEWS_INFO_SEND)) {
      return true;
    }
    if (eventType.equals(EventType.FAIL_SEND_NEWS)) {
      return true;
    }

    return false;
  }

  @Override
  public void sendMessage(String message, EventType eventType) {
    switch (eventType) {
      case NEWS_INFO_SEND -> {
        sendNews(message);
      }
      case FAIL_SEND_NEWS -> {
        sendNewsDLQ(message);
      }
    }
  }

  // 정상 케이스 전송
  private void sendNews(String message) {
    try {
      normalKafkaTemplate.send(newsTopicName, message)
          .thenAccept(result -> {
          }).exceptionally(ex -> {
            log.error("Fail Send Message [{}] : {}", newsTopicName, message, ex);
            sendNewsDLQ(message);
            return null;
          });

    } catch (Exception e) {
      log.error("Fail Send Message [{}] : {}", newsTopicName, message, e);
      sendNewsDLQ(message);
    }
  }

  // 실패 케이스 전송
  private void sendNewsDLQ(String message) {
    try {
      exceptionKafkaTemplate.send(newsExceptionTopicName, message)
          .thenAccept(result -> {
          }).exceptionally(ex -> {
            // DLQ 전송 실패는 로그만 남긴다.
            log.error("Fail Send DLQ Message [{}] : message={}", newsExceptionTopicName, message, ex);
            return null;
          });

    } catch (Exception e) {
      // DLQ 전송 실패는 로그만 남긴다.
      log.error("Fail Send DLQ Message [{}] : message={}", newsExceptionTopicName, message, e);
    }
  }
}
