package com.newpick4u.client.advertisement.application.message.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

public interface PointUpdateConsumer {

  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment)
      throws JsonProcessingException;
}
