package com.newpick4u.client.advertisement.application.message.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

public interface RetryPointUpdateConsumer {

  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment);
}
