package com.newpick4u.client.advertisement.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.client.advertisement.application.message.consumer.RetryPointUpdateConsumer;
import com.newpick4u.client.advertisement.application.message.request.PointUpdateMessage;
import com.newpick4u.client.advertisement.application.usecase.AdvertisementService;
import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryPointUpdateConsumerImpl implements RetryPointUpdateConsumer {

  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INT_BYTE_SIZE = 4;
  private static final String RETRY_HEADER_KEY = "kafka_deliveryAttempt";
  private final ObjectMapper objectMapper;
  private final AdvertisementService advertisementService;

  @KafkaListener(
      topics = "${spring.kafka.consumer.groups.point-request-dlq}",
      groupId = "${spring.kafka.consumer.groups.point-request-dlq}",
      containerFactory = "updateMessageConcurrentKafkaListenerContainerFactory"
  )
  @Override
  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {

    int retryAttempt = getRetryAttempt(record);
    try {
      PointUpdateMessage message = objectMapper.readValue(record.value(), PointUpdateMessage.class);
      log.info("[DLQ Retry] 포인트 DLQ 재처리 시작:{}", message);
      advertisementService.updatePointCounter(message);
      acknowledgment.acknowledge();
    } catch (Exception e) {
      if (retryAttempt > MAX_ATTEMPT_COUNT) {
        log.error("[DLQ Retry] 최대 재시도 횟수 초과로 인한 메시지 폐기");
        acknowledgment.acknowledge();
        return;
      }
      log.error("[DLQ Retry] 포인트 DLQ 재처리 실패", e);
    }
  }

  private int getRetryAttempt(ConsumerRecord<?, ?> record) {
    return Optional.ofNullable(record.headers().lastHeader(RETRY_HEADER_KEY))
        .map(Header::value)
        .filter(value -> value.length >= INT_BYTE_SIZE)
        .map(value -> ByteBuffer.wrap(value).getInt())
        .orElse(1);
  }
}
