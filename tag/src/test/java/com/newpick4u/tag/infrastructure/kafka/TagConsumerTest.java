package com.newpick4u.tag.infrastructure.kafka;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.tag.application.dto.AiNewsDto;
import com.newpick4u.tag.domain.entity.Tag;
import com.newpick4u.tag.domain.repository.TagRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"dev.news.fct.news.v1", "dev.news.del.news.v1",
    "dev.tag.fct.dql.v1"})
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
class TagConsumerTest {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TagRepository tagRepository;

  private AiNewsDto aiNewsDto;

  @Value("${spring.kafka.consumer.topic.create}")
  private String CREATE_TAG_TOPIC;
  @Value("${spring.kafka.consumer.topic.delete}")
  private String DELETE_TAG_TOPIC;

  @BeforeAll
  void init() {
    Tag tag = Tag.create("IT");
    tag.increaseScore();
    tagRepository.save(tag);
    tagRepository.save(Tag.create("TEST"));
  }

  @BeforeEach
  void setUp() {
    aiNewsDto = new AiNewsDto(UUID.randomUUID(), List.of("IT", "AI"));
  }

  @DisplayName("태그 생성 테스트-성공")
  @Test
  void testConsumer_success() throws Exception {

    String message = objectMapper.writeValueAsString(aiNewsDto);
    kafkaTemplate.send(CREATE_TAG_TOPIC, message);

    await()
        .atMost(Duration.ofSeconds(50))
        .pollInterval(Duration.ofMillis(1000)) // 1000ms 마다 검사
        .untilAsserted(() -> {
          Tag tag = tagRepository.findByTagName("AI")
              .orElseThrow(() -> new IllegalStateException("태그 저장 안 됨"));
          assertThat(tag.getTagName()).isEqualTo("AI");
        });
  }

  @DisplayName("뉴스 삭제 시 score가 1인 관련 태그들 삭제")
  @Test
  void deleteTag_Tag_count_is_1() throws Exception {

    AiNewsDto testDto = new AiNewsDto(UUID.randomUUID(), List.of("TEST"));
    String message = objectMapper.writeValueAsString(testDto);
    kafkaTemplate.send(DELETE_TAG_TOPIC, message);

    await()
        .atMost(Duration.ofSeconds(60))      // 최대 60초 기다림
        .pollInterval(Duration.ofMillis(1000)) // 1초 마다 검사
        .untilAsserted(() -> {
          List<Tag> findList = tagRepository.findAll();
          assertThat(findList.size()).isEqualTo(2); // 단일 테스트에서는 1 , 전체 테스트 돌릴 때는 2
        });
  }

  @DisplayName("뉴스 삭제 시 score가 2이상인 관련 태그들 감소")
  @Test
  void deleteTag_Tag_over_count_1() throws Exception {

    AiNewsDto testDto = new AiNewsDto(UUID.randomUUID(), List.of("IT"));
    String message = objectMapper.writeValueAsString(testDto);
    kafkaTemplate.send(DELETE_TAG_TOPIC, message);

    await()
        .atMost(Duration.ofSeconds(60))      // 최대 60초 기다림
        .pollInterval(Duration.ofMillis(1000)) // 1초 마다 검사
        .untilAsserted(() -> {
          Tag tag = tagRepository.findByTagName("TEST").get();
          assertThat(tag.getScore()).isEqualTo(1);
        });
  }
}