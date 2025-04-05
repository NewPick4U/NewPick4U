package com.newpick4u.newsorigin.newsorigin.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class NewsOriginServiceImplTest {

  @Autowired
  NewsOriginServiceImpl newsOriginServiceImpl;

  @Value("${app.client.naver.collect-news.search-display}")
  private String searchDisplay;

  @Test
  @DisplayName("뉴스 정보 수집 테스트")
  void collectOriginNewsTest() {
//    int collectCount = newsOriginServiceImpl.collectOriginNews();
//
//    Integer inputCount = Integer.valueOf(searchDisplay);
//
//    Assertions.assertEquals(inputCount, collectCount);
  }

}