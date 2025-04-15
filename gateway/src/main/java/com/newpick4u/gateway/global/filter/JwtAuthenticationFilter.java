package com.newpick4u.gateway.global.filter;

import com.newpick4u.common.exception.CustomException;
import com.newpick4u.gateway.global.exception.GatewayErrorCode;
import com.newpick4u.gateway.global.util.TokenProvider;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

  private static final String USER_ID_HEADER = "X-User-Id";
  private static final String USER_ROLE_HEADER = "X-User-Role";
  private final TokenProvider tokenProvider;

  private static ServerHttpRequest createCustomRequest(ServerWebExchange exchange, String userId,
      String userRole) {
    return exchange.getRequest().mutate()
        .header(USER_ID_HEADER, userId)
        .header(USER_ROLE_HEADER, userRole)
        .build();
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String accessToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    log.info("[JwtAuthenticationFilter]{}", accessToken);

    if (tokenProvider.validAccessToken(accessToken)) {
      accessToken = accessToken.substring(7);
      String userId = tokenProvider.getUserId(accessToken);
      String userRole = tokenProvider.getUserRole(accessToken);
      ServerHttpRequest modifiedRequest = createCustomRequest(exchange, userId, userRole);
      ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
      return chain.filter(modifiedExchange);
    }
    throw CustomException.from(GatewayErrorCode.EXPIRED_JWT_TOKEN);
  }
}
