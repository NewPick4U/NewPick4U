package com.newpick4u.news.news.infrastructure.kafka;

import com.newpick4u.news.news.application.dto.NewsTagDto;
import com.newpick4u.news.news.application.usecase.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryNewsTagConsumer {

    private final NewsService newsService;

    @KafkaListener(
            topics = "tag-dlq.fct.v1",
            groupId = "news-tag-dlq-consumer",
            containerFactory = "newsTagListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, NewsTagDto> record, Acknowledgment ack) {
        NewsTagDto dto = record.value();
        try {
            log.info("[DLQ Retry] 태그 DLQ 재처리 시작: {}", dto);
            newsService.updateNewsTagList(dto);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[DLQ Retry] 태그 재처리 실패 - 메시지: {}", dto, e);
            throw new RuntimeException(e);
        }
    }
}
