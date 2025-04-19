package com.newpick4u.newsorigin.newsorigin.infrastructure.client;

import com.newpick4u.newsorigin.newsorigin.application.GetOriginBodyClient;
import javax.net.ssl.SSLHandshakeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class GetOriginBodyClientImpl implements GetOriginBodyClient {

  private final WebClient webClient;

  public String getOriginNewsBody(String targetUrl) {
    String getBodyResult = webClient.get()
        .uri(targetUrl)
        .retrieve()
        .bodyToMono(String.class)
        .onErrorResume(throwable -> {
          // SSL 실패 케이스는 예외로그 skip
          if (throwable instanceof WebClientRequestException) {
            Throwable cause = throwable.getCause();
            if (cause instanceof SSLHandshakeException) {
              log.warn("SSL Fail : Drop : url = {}", targetUrl);
              return Mono.empty();
            }
          }

          log.error("Fail : url = {}", targetUrl, throwable);
          return Mono.empty();
        })
        .block();
    return getBodyResult;
  }

}
