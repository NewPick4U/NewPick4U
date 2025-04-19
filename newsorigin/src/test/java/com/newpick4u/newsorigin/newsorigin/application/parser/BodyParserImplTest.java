package com.newpick4u.newsorigin.newsorigin.application.parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@MockitoBean(types = ScheduledAnnotationBeanPostProcessor.class) // @Scheduled 무력화
@ActiveProfiles("test")
@SpringBootTest
class BodyParserImplTest {

  @Autowired
  BodyParserImpl bodyParser;

  @Test
  @DisplayName("본문 추출 테스트 : 기사 1 구조 대상 ")
  void extractMainBodyTest() throws IOException, URISyntaxException {
    // 리소스 경로 가져오기
    URL resourceUrl = getClass().getClassLoader().getResource("html-sample.html");

    // URI를 통해 안전하게 Path 생성
    Path filePath = Paths.get(resourceUrl.toURI());

    // 파일 읽기
    String html = Files.readString(filePath, StandardCharsets.UTF_8);
    String title = "JK김동욱·이동욱, <b>정치</b>적 발언 논란…네티즌 반응 분분";
    String result = bodyParser.extractMainBody(html, title);
    // log.info("result = {}", result);
  }

  @Test
  @DisplayName("본문 추출 테스트 : 기사 2 구조 대상 ")
  void extractMainBodyTest2() throws IOException, URISyntaxException {
    // 리소스 경로 가져오기
    URL resourceUrl = getClass().getClassLoader().getResource("html-sample2.html");

    // URI를 통해 안전하게 Path 생성
    Path filePath = Paths.get(resourceUrl.toURI());

    // 파일 읽기
    String html = Files.readString(filePath, StandardCharsets.UTF_8);
    String title = "[초점] 테슬라, '전례 없는 브랜드 훼손'에 실적 전망 하향";
    String result = bodyParser.extractMainBody(html, title);
    // log.info("result = {}", result);
  }
}