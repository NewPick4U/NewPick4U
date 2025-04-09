package com.newpick4u.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.news.news.application.dto.NewsInfoDto;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.entity.NewsStatus;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"news-info.fct.v1", "news-info-dlq.fct.v1"})
@DirtiesContext
@ActiveProfiles("test")
class NewsInfoConsumerTest {

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
        kafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer()));
    }

    @Test
    void 뉴스_초안_정상_컨슈밍_처리() throws Exception {
        // given
        NewsInfoDto dto = new NewsInfoDto("ai-123", "제목", "내용");
        String json = objectMapper.writeValueAsString(dto);

        // when
        kafkaTemplate.send("news-info.fct.v1", "ai-123", json);

        // then
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    News news = newsRepository.findByAiNewsId("ai-123").orElseThrow();
                    assertThat(news.getTitle()).isEqualTo("제목");
                    assertThat(news.getStatus()).isEqualTo(NewsStatus.PENDING);
                });
    }

    @Test
    void 뉴스_초안_DLQ_리트라이_1회성공() throws Exception {
        // given
        NewsInfoDto dto = new NewsInfoDto("fail-once", "재시도 제목", "재시도 내용");
        String json = objectMapper.writeValueAsString(dto);

        // when
        kafkaTemplate.send("news-info.fct.v1", "fail-once", json);

        // then
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            News news = newsRepository.findByAiNewsId("fail-once").orElseThrow();
            assertThat(news.getTitle()).isEqualTo("재시도 제목");
            assertThat(news.getStatus()).isEqualTo(NewsStatus.PENDING);
        });
    }

    @Test
    void 뉴스_초안_DLQ_3회실패_최종실패_및_DLQ확인() throws Exception {
        // given
        NewsInfoDto dto = new NewsInfoDto("fail-me", "DLQ 제목", "DLQ 내용");
        String json = objectMapper.writeValueAsString(dto);

        // when
        kafkaTemplate.send("news-info.fct.v1", "fail-me", json);

        // then: DLQ 도착 여부까지 기다리기
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-dlq-check", "true", embeddedKafkaBroker);
            Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                    consumerProps, new StringDeserializer(), new StringDeserializer()).createConsumer();
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "news-info-dlq.fct.v1");

            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
            boolean foundInDlq = StreamSupport.stream(records.spliterator(), false)
                    .anyMatch(record -> record.key().equals("fail-me"));

            assertThat(foundInDlq).isTrue();
        });

        // and: DB에 저장 안됐는지
        assertThat(newsRepository.findByAiNewsId("fail-me")).isEmpty();
    }
}
