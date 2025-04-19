package com.newpick4u.ainews.global.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignGeminiClientConfig {

  private static final String CONTENT_TYPE_KEY = "Content-Type";
  private static final String CONTENT_TYPE_VALUE = "application/json";

  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
      requestTemplate.header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
    };
  }
}
