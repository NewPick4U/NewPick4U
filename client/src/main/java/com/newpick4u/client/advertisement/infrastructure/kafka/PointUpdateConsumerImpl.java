package com.newpick4u.client.advertisement.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.client.advertisement.application.message.consumer.PointUpdateConsumer;
import com.newpick4u.client.advertisement.application.message.request.PointUpdateMessage;
import com.newpick4u.client.advertisement.application.usecase.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PointUpdateConsumerImpl implements PointUpdateConsumer {

  private final ObjectMapper objectMapper;
  private final AdvertisementService advertisementService;

  @KafkaListener(topics = "${spring.kafka.consumer.topics.point-request}",
      groupId = "${spring.kafka.consumer.groups.point-request}",
      containerFactory = "updateMessageConcurrentKafkaListenerContainerFactory"
  )
  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment)
      throws JsonProcessingException {
    try {
      PointUpdateMessage message = objectMapper.readValue(record.value(), PointUpdateMessage.class);
      advertisementService.updatePointCounter(message);
      acknowledgment.acknowledge();
    } catch (Exception e) {
      log.error("Kafka consume error", e);
    }
  }
}
