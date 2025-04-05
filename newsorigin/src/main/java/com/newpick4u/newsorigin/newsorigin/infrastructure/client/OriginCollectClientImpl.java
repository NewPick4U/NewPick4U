package com.newpick4u.newsorigin.newsorigin.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.newsorigin.global.common.CommonUtil;
import com.newpick4u.newsorigin.newsorigin.application.OriginCollectClient;
import com.newpick4u.newsorigin.newsorigin.application.dto.NewNewsOriginDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OriginCollectClientImpl implements OriginCollectClient {

  private final NaverFeignClient naverFeignClient;

  @Value("${app.client.naver.collect-news.search-query}")
  private String searchQuery;

  @Value("${app.client.naver.collect-news.search-sort}")
  private String searchSort;

  @Value("${app.client.naver.collect-news.search-display}")
  private String searchDisplay;

  public ArrayList<NewNewsOriginDto> getOriginNewsList() throws JsonProcessingException {

    Map<String, String> map = getRequestParam();

    String searchResult = naverFeignClient.getSearchResult(map);
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode searchResultJson = objectMapper.readTree(searchResult);

    String lastBuildDate = searchResultJson.get("lastBuildDate").asText();
    log.info("네이버측 마지막 업데이트 시각 = {}", lastBuildDate);

    JsonNode items = searchResultJson.get("items");

    ArrayList<NewNewsOriginDto> newNewsOriginDtoList = new ArrayList<>();
    for (JsonNode item : items) {

      String url = item.get("originallink").asText();

      String pubDateString = item.get("pubDate").asText();
      LocalDateTime publishedDate = CommonUtil.convertStringToLocalDateTime(pubDateString);

      NewNewsOriginDto newNewsOriginDto = new NewNewsOriginDto(publishedDate, url);
      newNewsOriginDtoList.add(newNewsOriginDto);
    }
    return newNewsOriginDtoList;
  }

  private Map<String, String> getRequestParam() {
    return Map.of(
        "query", searchQuery,
        "sort", searchSort,
        "display", searchDisplay
    );
  }
}
