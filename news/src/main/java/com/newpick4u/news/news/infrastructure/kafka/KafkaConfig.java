package com.newpick4u.news.news.infrastructure.kafka;

import com.newpick4u.news.news.application.dto.NewsInfoDto;
import com.newpick4u.news.news.application.dto.NewsTagDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final String PACKAGE_TRUSTED = "com.newpick4u.news.news.application.dto";

    @Value("${KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrapServers;

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    private Map<String, Object> commonConsumerProps(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    private <T> ConsumerFactory<String, T> buildConsumerFactory(Class<T> clazz, String groupId) {
        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(clazz);
        valueDeserializer.addTrustedPackages(PACKAGE_TRUSTED);
        valueDeserializer.setRemoveTypeHeaders(false);
        valueDeserializer.setUseTypeMapperForKey(true);
        return new DefaultKafkaConsumerFactory<>(
                commonConsumerProps(groupId),
                new StringDeserializer(),
                valueDeserializer
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> buildListenerContainerFactory(
            ConsumerFactory<String, T> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    // ai - news 카프카
    @Bean
    public ConsumerFactory<String, NewsInfoDto> newsInfoConsumerFactory() {
        return buildConsumerFactory(NewsInfoDto.class, "news-info-consumer");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NewsInfoDto> newsInfoListenerContainerFactory(
            ConsumerFactory<String, NewsInfoDto> newsInfoConsumerFactory,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        var factory = buildListenerContainerFactory(newsInfoConsumerFactory);

        // DLQ 적용
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition("news-info-dlq.fct.v1", record.partition()));
        var errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }


    // tag - news 카프카
    @Bean
    public ConsumerFactory<String, NewsTagDto> newsTagConsumerFactory() {
        return buildConsumerFactory(NewsTagDto.class, "news-tag-consumer");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NewsTagDto> newsTagListenerContainerFactory(
            ConsumerFactory<String, NewsTagDto> newsTagConsumerFactory,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        var factory = buildListenerContainerFactory(newsTagConsumerFactory);

        // DLQ 적용
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition("tag-dlq.fct.v1", record.partition()));
        var errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

}
