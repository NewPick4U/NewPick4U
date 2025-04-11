package com.newpick4u.client.advertisement.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;
  @Value("${spring.kafka.consumer.groups.point-request}")
  private String groupId;
  @Value("${spring.kafka.consumer.auto-offset-reset}")
  private String autoOffsetReset;
  @Value("${spring.kafka.consumer.enable-auto-commit}")
  private boolean enableAutoCommit;

  private Map<String, Object> commonConsumerProps(String groupId) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
    return props;
  }

  private ConsumerFactory<String, String> buildConsumerFactory(String groupId) {
    return new DefaultKafkaConsumerFactory<>(
        commonConsumerProps(groupId),
        new StringDeserializer(),
        new StringDeserializer() // 값을 문자열로 받도록 변경
    );
  }

  @Bean
  public ConsumerFactory<String, String> pointRequestConsumerFactory() {
    return buildConsumerFactory(groupId);
  }

  private <T> ConcurrentKafkaListenerContainerFactory<String, String> buildListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory
  ) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  private Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // JSON 직렬화기 사용
    return props;
  }

  @Bean
  public ProducerFactory<String, String> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> updateMessageConcurrentKafkaListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory,
      KafkaTemplate<String, String> kafkaTemplate,
      @Value("${spring.kafka.consumer.groups.point-request-dlq}") String topic) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = buildListenerContainerFactory(
        consumerFactory);

    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
        kafkaTemplate,
        (record, ex) -> new TopicPartition(topic, record.partition()));
    DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer,
        new FixedBackOff(1000L, 3));
    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }

}
