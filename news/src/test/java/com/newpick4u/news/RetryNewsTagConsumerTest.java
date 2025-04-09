package com.newpick4u.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.news.news.application.dto.NewsInfoDto;
import com.newpick4u.news.news.application.dto.NewsTagDto;
import com.newpick4u.news.news.application.dto.NewsTagDto.TagDto;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.entity.NewsStatus;
import com.newpick4u.news.news.domain.entity.NewsTag;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"tag-dlq.fct.v1", "news-info.fct.v1"})
@DirtiesContext
@ActiveProfiles("test")
class RetryNewsTagConsumerTest {

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
    void DLQ_태그_재처리_정상_수행() throws Exception {
        // given - 뉴스 먼저 저장
        String aiNewsId = "dlq-tag-123";
        NewsInfoDto newsDto = new NewsInfoDto(aiNewsId, "DLQ 태그 제목", "DLQ 태그 내용");
        String newsJson = objectMapper.writeValueAsString(newsDto);
        kafkaTemplate.send("news-info.fct.v1", aiNewsId, newsJson);
        Awaitility.await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    News news = newsRepository.findByAiNewsId(aiNewsId).orElseThrow();
                    assertThat(news.getTitle()).isEqualTo("DLQ 태그 제목");
                });

        // 태그 DTO 구성
        List<TagDto> tagList = List.of(
                new TagDto(UUID.randomUUID(), "정치"),
                new TagDto(UUID.randomUUID(), "경제"),
                new TagDto(UUID.randomUUID(), "사회"),
                new TagDto(UUID.randomUUID(), "문화"),
                new TagDto(UUID.randomUUID(), "과학"),
                new TagDto(UUID.randomUUID(), "기술"),
                new TagDto(UUID.randomUUID(), "교육"),
                new TagDto(UUID.randomUUID(), "스포츠"),
                new TagDto(UUID.randomUUID(), "여행"),
                new TagDto(UUID.randomUUID(), "연예")
        );
        NewsTagDto tagDto = new NewsTagDto(aiNewsId, tagList);
        String tagJson = objectMapper.writeValueAsString(tagDto);

        // when - DLQ 토픽에 전송
        kafkaTemplate.send(new ProducerRecord<>("tag-dlq.fct.v1", aiNewsId, tagJson));

        // then
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    News news = newsRepository.findByAiNewsId(aiNewsId).orElseThrow();
                    List<NewsTag> tags = news.getNewsTagList();
                    assertThat(tags).hasSize(10);
                    assertThat(news.getStatus()).isEqualTo(NewsStatus.ACTIVE);
                });
    }
}
