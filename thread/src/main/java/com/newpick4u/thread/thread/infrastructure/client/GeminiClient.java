package com.newpick4u.thread.thread.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.thread.thread.application.usecase.AiClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GeminiClient implements AiClient {

  @Value("${gemini.url}")
  private String geminiUrl;

  private final RestTemplate restTemplate;

  @Override
  public String analyzeSummary(UUID threadId, List<String> commentList) {
    // Gemini API 호출 로직 (prompt 구성 → REST 호출 → 결과 반환)
    // 예: "이 쓰레드에 대한 여론을 한 문장으로 요약해줘"
    String prompt = buildPrompt(commentList);
    return callGeminiApi(prompt); // 또는 HTTP 요청 등
  }

  private String buildPrompt(List<String> commentList) {
    String joined = String.join("\n", commentList);
    return "다음 댓글을 보고 각 반응별 여론 비율을 결론만 알려줘:\n" + joined;
  }

  private String callGeminiApi(String prompt) {
    // 요청 본문 구성
    Map<String, Object> requestBody = Map.of(
        "contents", List.of(
            Map.of("parts", List.of(Map.of("text", prompt)))
        )
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(geminiUrl, request,
          String.class);

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode root = objectMapper.readTree(response.getBody());

      JsonNode candidates = root.path("candidates");
      if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
        return null;
      }

      JsonNode textNode = candidates.get(0)
          .path("content")
          .path("parts").get(0)
          .path("text");

      return textNode.isTextual() ? textNode.asText() : null;

    } catch (Exception e) {
      throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
    }
  }
}
