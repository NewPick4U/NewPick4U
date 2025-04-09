package com.newpick4u.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.news.news.application.dto.NewsTagDto;
import com.newpick4u.news.news.application.usecase.TagInboxScheduler;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.entity.TagInbox;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import com.newpick4u.news.news.domain.repository.TagInboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TagInboxSchedulerTest {

    @Autowired
    private TagInboxScheduler tagInboxScheduler;

    @Autowired
    private TagInboxRepository tagInboxRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tagInboxRepository.findAll().forEach(tagInboxRepository::delete);
    }

    @Test
    void 스케줄러가_뉴스와_인박스_매핑_정상작동() throws Exception {
        // given
        News news = News.create("ai-xyz", "제목", "내용", 0L);
        newsRepository.save(news);

        NewsTagDto dto = new NewsTagDto("ai-xyz", createSampleTags());
        String payload = objectMapper.writeValueAsString(dto);
        TagInbox inbox = TagInbox.create("ai-xyz", payload);
        tagInboxRepository.save(inbox);

        // when
        tagInboxScheduler.processInbox();

        // then
        News updatedNews = newsRepository.findByAiNewsId("ai-xyz").orElseThrow();
        assertThat(updatedNews.getNewsTagList()).hasSize(10);
        assertThat(tagInboxRepository.findAll()).isEmpty(); // 처리 후 삭제
    }

    @Test
    void 뉴스가_없으면_인박스_그대로_유지됨() throws Exception {
        // given
        NewsTagDto dto = new NewsTagDto("ai-missing", createSampleTags());
        String payload = new ObjectMapper().writeValueAsString(dto);
        TagInbox inbox = TagInbox.create("ai-missing", payload);
        tagInboxRepository.save(inbox);

        // when
        tagInboxScheduler.processInbox();

        // then
        assertThat(tagInboxRepository.findAll()).hasSize(1); // 삭제되지 않음
    }

    private List<NewsTagDto.TagDto> createSampleTags() {
        return List.of(
                new NewsTagDto.TagDto(UUID.randomUUID(), "정치"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "경제"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "사회"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "문화"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "연예"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "IT"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "국제"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "환경"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "교육"),
                new NewsTagDto.TagDto(UUID.randomUUID(), "스포츠")
        );
    }
}
