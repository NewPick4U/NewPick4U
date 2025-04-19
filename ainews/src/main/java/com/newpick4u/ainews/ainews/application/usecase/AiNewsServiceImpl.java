package com.newpick4u.ainews.ainews.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.ainews.ainews.application.AiClient;
import com.newpick4u.ainews.ainews.application.EventPublisher;
import com.newpick4u.ainews.ainews.application.EventType;
import com.newpick4u.ainews.ainews.application.dto.NewsInfoDto;
import com.newpick4u.ainews.ainews.application.dto.NewsOriginDto;
import com.newpick4u.ainews.ainews.application.dto.ProceedAiNewsDto;
import com.newpick4u.ainews.ainews.application.dto.TagInfoDto;
import com.newpick4u.ainews.ainews.domain.entity.AiNews;
import com.newpick4u.ainews.ainews.domain.repository.AiNewsRepository;
import com.newpick4u.ainews.global.exception.NoRemainRequestCountException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AiNewsServiceImpl
    implements AiNormalEventHandleService, AiExceptionEventHandleService {

  private final AiClient aiClient;
  private final AiNewsRepository aiNewsRepository;
  private final List<EventPublisher> eventPublishers;
  private final ObjectMapper objectMapper;

  @Value("${app.tag.max-size:10}")
  private int MAX_TAG_SIZE;

  @Override
  public void processAiNews(String originalMessage) {
    try {
      NewsOriginDto newsOriginDto = objectMapper.readValue(originalMessage, NewsOriginDto.class);
      ProceedAiNewsDto proceedAiNewsDto = getProceedAiNewsFromApi(originalMessage, newsOriginDto);
      if (proceedAiNewsDto == null) {
        return;
      }

      saveAndSendTask(newsOriginDto, proceedAiNewsDto);

    } catch (JsonProcessingException jpe) {
      // json 관련 예외는 재처리 안함
      log.error("Json Processing Exception -> Never Retry : target={}", originalMessage, jpe);
    }
  }

  private void saveAndSendTask(NewsOriginDto newsOriginDto, ProceedAiNewsDto proceedAiNewsDto)
      throws JsonProcessingException {

    AiNews aiNews = createAiNewsByDto(newsOriginDto, proceedAiNewsDto);

    startProcess(aiNews, proceedAiNewsDto.getTagStringListByMaxSize(MAX_TAG_SIZE));
  }

  @Override
  public void saveAndSendTaskByListener(String aiNewsString)
      throws JsonProcessingException {

    AiNews aiNews = createAiNewsByJsonString(aiNewsString);

    startProcess(aiNews, aiNews.getTagList());
  }

  private void startProcess(AiNews aiNews, List<String> tags) throws JsonProcessingException {
    // DB Save
    AiNews savedAiNews = saveAiNews(aiNews);
    if (savedAiNews == null) {
      return;
    }

    // send -> Tag Service
    sendTagMessage(savedAiNews.getId(), tags);

    // send -> News Service
    sendNewsMessage(savedAiNews);
  }

  private AiNews saveAiNews(AiNews aiNews)
      throws JsonProcessingException {
    AiNews savedAiNews;
    try {
      savedAiNews = aiNewsRepository.save(aiNews);
    } catch (Exception e) {
      String aiNewsJson = objectMapper.writeValueAsString(aiNews);
      log.error("Save Fail -> Send DLQ : {}", aiNewsJson, e);
      sendMessage(aiNewsJson, EventType.FAIL_SAVE_AINEWS);
      return null;
    }
    return savedAiNews;
  }

  private void sendTagMessage(UUID aiNewsId, List<String> tags) throws JsonProcessingException {
    TagInfoDto tagInfoDto = TagInfoDto.of(tags, aiNewsId);
    String tagsJson = objectMapper.writeValueAsString(tagInfoDto);
    sendMessage(tagsJson, EventType.TAG_INFO_SEND);
  }

  private void sendNewsMessage(AiNews aiNews) throws JsonProcessingException {
    NewsInfoDto newsInfoDto = NewsInfoDto.of(
        aiNews.getId(),
        aiNews.getTitle(),
        aiNews.getSummary(),
        aiNews.getUrl(),
        aiNews.getPublishedDate()
    );

    String newsInfoJson = objectMapper.writeValueAsString(newsInfoDto);
    sendMessage(newsInfoJson, EventType.NEWS_INFO_SEND);
  }

  private ProceedAiNewsDto getProceedAiNewsFromApi(String originalMessage,
      NewsOriginDto newsOriginDto)
      throws JsonProcessingException {
    ProceedAiNewsDto proceedAiNewsDto;
    try {
      proceedAiNewsDto = aiClient.processByAiApi(newsOriginDto.body());

    } catch (JsonProcessingException jpe) {
      throw jpe;

    } catch (NoRemainRequestCountException nrqce) {
      log.warn(nrqce.getMessage());
      sendMessage(originalMessage, EventType.FAIL_PROCESS_AINEWS);
      return null;

    } catch (Exception e) {
      log.error("", e);
      sendMessage(originalMessage, EventType.FAIL_PROCESS_AINEWS);
      return null;
    }

    return proceedAiNewsDto;
  }

  private AiNews createAiNewsByDto(NewsOriginDto newsOriginDto,
      ProceedAiNewsDto proceedAiNewsDto) {
    AiNews aiNews = AiNews.create(
        UUID.fromString(newsOriginDto.originNewsId()),
        newsOriginDto.url(),
        newsOriginDto.title(),
        proceedAiNewsDto.proceedFields().getTagsString(MAX_TAG_SIZE),
        proceedAiNewsDto.proceedFields().summary(),
        newsOriginDto.publishedDate(),
        proceedAiNewsDto.originalString()
    );
    return aiNews;
  }

  private AiNews createAiNewsByJsonString(String aiNewsString)
      throws JsonProcessingException {
    AiNews aiNews = objectMapper.readValue(aiNewsString, AiNews.class);
    return aiNews;
  }

  private void sendMessage(String message, EventType eventType) {
    this.eventPublishers.stream()
        .filter(ep -> ep.isSupport(eventType))
        .findAny()
        .orElseThrow(() -> new RuntimeException("Not Support EventType : " + eventType.name()))
        .sendMessage(message, eventType);
  }
}
