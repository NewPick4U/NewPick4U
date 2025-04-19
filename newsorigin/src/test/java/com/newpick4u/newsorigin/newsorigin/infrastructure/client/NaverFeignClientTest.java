package com.newpick4u.newsorigin.newsorigin.infrastructure.client;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@MockitoBean(types = ScheduledAnnotationBeanPostProcessor.class) // @Scheduled 무력화
@ActiveProfiles("test")
@SpringBootTest
class NaverFeignClientTest {

  @Autowired
  NaverFeignClient naverFeignClient;

  @Value("${app.client.naver.collect-news.search-query}")
  private String searchQuery;

  @Value("${app.client.naver.collect-news.search-sort}")
  private String searchSort;

  @Value("${app.client.naver.collect-news.search-display}")
  private String searchDisplay;

  @Test
  @DisplayName("네이버 API 호출 테스트")
  void callAPITest() {
//    String searchResult = naverFeignClient.getSearchResult(getRequestParam());
//    log.info("result: {}", searchResult);
  }

  private Map<String, String> getRequestParam() {
    return Map.of(
        "query", "우주 수장의 워싱턴 출장",
        "sort", searchSort,
        "display", searchDisplay
    );
  }

}