package com.newpick4u.newsorigin.global.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignNaverClientConfig {

  private static final String NAVER_ID_HEADER = "X-Naver-Client-Id";
  private static final String NAVER_KEY_HEADER = "X-Naver-Client-Secret";
  @Value("${app.client.naver.collect-news.id}")
  private String id;
  @Value("${app.client.naver.collect-news.key}")
  private String key;

  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
      requestTemplate.header(NAVER_ID_HEADER, id);
      requestTemplate.header(NAVER_KEY_HEADER, key);
    };
  }
}
