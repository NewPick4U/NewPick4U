package com.newpick4u.ainews.ainews.infrastructure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.ainews.ainews.application.AiClient;
import com.newpick4u.ainews.ainews.application.dto.ProceedAiNewsDto;
import com.newpick4u.ainews.ainews.application.dto.ProceedAiNewsDto.ProceedFields;
import com.newpick4u.ainews.ainews.domain.entity.NewsCategory;
import com.newpick4u.ainews.ainews.infrastructure.ai.dto.GeminiRequestDto;
import com.newpick4u.ainews.ainews.infrastructure.ai.dto.GeminiResponseDto;
import com.newpick4u.ainews.global.exception.NoRemainRequestCountException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GeminiClient implements AiClient {

  private final ObjectMapper objectMapper;
  private final GeminiFeignClient geminiFeignClient;

  @Value("${app.client.gemini.max-request-count-per-min:15}")
  private int MAX_AVAILABLE_REQUEST_COUNT_PER_MIN;

  @Value("${app.client.gemini.key}")
  private String API_KEY;

  @Value("${app.client.gemini.request-body-base}")
  private String REQUEST_BODY_BASE;

  private int remainAvailableRequestCountPerMin;

  @PostConstruct
  public void init() {
    initRemainAvailableRequestCountPerMin();
  }

  @Override
  public void initRemainAvailableRequestCountPerMin() {
    remainAvailableRequestCountPerMin = MAX_AVAILABLE_REQUEST_COUNT_PER_MIN;
  }

  @Override
  synchronized public ProceedAiNewsDto processByAiApi(String newsBody)
      throws JsonProcessingException {
    if (remainAvailableRequestCountPerMin <= 0) {
      throw new NoRemainRequestCountException();
    }

    String geminiResponseString = geminiFeignClient.processGemini(
        API_KEY,
        GeminiRequestDto.of(
            getResultRequestBody(newsBody)
        )
    );

    ProceedAiNewsDto proceedAiNewsDto = getProceedAiNewsDto(geminiResponseString);
    remainAvailableRequestCountPerMin--;

    return proceedAiNewsDto;
  }

  private ProceedAiNewsDto getProceedAiNewsDto(String geminiResponseString)
      throws JsonProcessingException {
    String parsedAnswerData = getAnswerFromResponse(geminiResponseString, objectMapper);
    
    ProceedFields proceedFields = objectMapper.readValue(parsedAnswerData, ProceedFields.class);

    ProceedAiNewsDto proceedAiNewsDto = ProceedAiNewsDto.of(geminiResponseString, proceedFields);
    return proceedAiNewsDto;
  }

  private String getAnswerFromResponse(String geminiResponseString, ObjectMapper objectMapper)
      throws JsonProcessingException {
    GeminiResponseDto geminiResponseDto = objectMapper.readValue(geminiResponseString,
        GeminiResponseDto.class);

    String parsedAnswerData = geminiResponseDto
        .getOnlyText()
        .replace("```json", "")
        .replace("```", "");

    return parsedAnswerData;
  }


  private String getResultRequestBody(String newsBody) {
    return REQUEST_BODY_BASE
        .replace("{category}", NewsCategory.getKoreanNames())
        .replace("{news-body}", newsBody);
  }
}
