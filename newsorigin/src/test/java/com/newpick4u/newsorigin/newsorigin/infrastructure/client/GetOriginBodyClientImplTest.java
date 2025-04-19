package com.newpick4u.newsorigin.newsorigin.infrastructure.client;

import com.newpick4u.newsorigin.newsorigin.application.parser.BodyParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@ActiveProfiles("test")
@MockitoBean(types = ScheduledAnnotationBeanPostProcessor.class) // @Scheduled 무력화
@SpringBootTest
class GetOriginBodyClientImplTest {

  @Autowired
  GetOriginBodyClientImpl getOriginBodyClient;

  @Autowired
  BodyParser bodyParser;

  @Test
  @DisplayName("신문 원문 획득 정상호출 테스트")
  void getOriginNewsBodyTest() {
//    String targetUrl = "http://www.g-enews.com/ko-kr/news/article/news_all/2025040519133638279a1f309431_1/article.html";
//    getOriginBodyClient.getOriginBody(targetUrl);
  }

  @Test
  @DisplayName("신문 원문 획득 ~ 획득 신문 파싱 확인")
  void getOriginNewsBodyCheckTest() {
//    String targetUrl = "https://www.chosun.com/politics/politics_general/2025/04/16/ZOIWGKROMBG5PBIZF4G5JPUTXA/?utm_source=naver&utm_medium=referral&utm_campaign=naver-news";
//    String originNewsBody = getOriginBodyClient.getOriginNewsBody(targetUrl);
//    log.info("originNewsBody = {}", originNewsBody);
//    String s = bodyParser.extractMainBody(originNewsBody, "‘4등 싸움’이 더 치열한 국힘 경선");
//    log.info("body : {}", s);
  }
}