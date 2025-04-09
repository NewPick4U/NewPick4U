package com.newpick4u.news;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.news.news.application.dto.NewsInfoDto;
import com.newpick4u.news.news.application.dto.NewsTagDto;
import com.newpick4u.news.news.domain.entity.News;
import com.newpick4u.news.news.domain.entity.TagInbox;
import com.newpick4u.news.news.domain.repository.NewsRepository;
import com.newpick4u.news.news.domain.repository.TagInboxRepository;
import com.newpick4u.news.news.application.usecase.NewsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private TagInboxRepository tagInboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NewsServiceImpl newsService;

    private final String aiNewsId = "ai-123";

    @Test
    void 뉴스가_존재하지_않으면_DB에_저장된다()
    {
        // given
        NewsInfoDto dto = new NewsInfoDto(aiNewsId, "제목", "내용");
        when(newsRepository.existsByAiNewsId(aiNewsId)).thenReturn(false);

        // when
        newsService.saveNewsInfo(dto);

        // then
        verify(newsRepository, times(1)).save(any(News.class));
    }

    @Test
    void 뉴스가_존재하면_태그를_매핑한다() {
        // given
        NewsTagDto.TagDto tagDto = new NewsTagDto.TagDto(UUID.randomUUID(), "정치");
        List<NewsTagDto.TagDto> tagList = List.of(tagDto, tagDto, tagDto, tagDto, tagDto, tagDto, tagDto, tagDto, tagDto, tagDto);
        NewsTagDto dto = new NewsTagDto(aiNewsId, tagList);
        News news = News.create(aiNewsId, "제목", "내용", 0L);

        when(newsRepository.findByAiNewsId(aiNewsId)).thenReturn(Optional.of(news));

        // when
        newsService.updateNewsTagList(dto);

        // then
        assertThat(news.getNewsTagList()).hasSize(10);
        verify(tagInboxRepository, never()).save(any()); // 인박스에 저장되지 않아야 함
    }

    @Test
    void 뉴스가_없으면_인박스에_저장된다() throws JsonProcessingException {
        // given
        NewsTagDto.TagDto tagDto = new NewsTagDto.TagDto(UUID.randomUUID(), "정치");
        List<NewsTagDto.TagDto> tagList = List.of(tagDto, tagDto, tagDto, tagDto, tagDto, tagDto, tagDto, tagDto, tagDto, tagDto);
        NewsTagDto dto = new NewsTagDto(aiNewsId, tagList);

        when(newsRepository.findByAiNewsId(aiNewsId)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(dto)).thenReturn("{...json...}");

        // when
        newsService.updateNewsTagList(dto);

        // then
        verify(tagInboxRepository, times(1)).save(any(TagInbox.class));
    }
}
