package com.newpick4u.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.news.news.application.dto.NewsInfoDto;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.entity.NewsStatus;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"news-info-dlq.fct.v1"})
@DirtiesContext
@ActiveProfiles("test")
class RetryNewsInfoConsumerTest {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setup() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
    }

    @Test
    void DLQ_뉴스_초안_재처리_정상_수행() throws Exception {
        // given
        String aiNewsId = "dlq-retry-123";
        NewsInfoDto dto = new NewsInfoDto(aiNewsId, "DLQ 재처리 제목", "DLQ 재처리 내용");
        String json = objectMapper.writeValueAsString(dto);

        // when
        kafkaTemplate.send(new ProducerRecord<>("news-info-dlq.fct.v1", aiNewsId, json));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    News news = newsRepository.findByAiNewsId(aiNewsId).orElseThrow();
                    assertThat(news.getTitle()).isEqualTo("DLQ 재처리 제목");
                });

        // then
        News news = newsRepository.findByAiNewsId(aiNewsId).orElseThrow();
        assertThat(news.getTitle()).isEqualTo("DLQ 재처리 제목");
        assertThat(news.getContent()).isEqualTo("DLQ 재처리 내용");
        assertThat(news.getStatus()).isEqualTo(NewsStatus.PENDING);
    }
}
