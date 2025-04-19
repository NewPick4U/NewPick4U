package com.newpick4u.thread.thread.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class GeminiClientTest {

  @Autowired
  private GeminiClient geminiClient;

  @MockitoBean
  private CommentClientImpl commentClient;

  @Test
  void givenComments_whenAnalyzeSummary_thenReturnsValidSummary() {
    // given
    UUID threadId = UUID.randomUUID();
    UUID threadId2 = UUID.randomUUID();
    UUID threadId3 = UUID.randomUUID();
    UUID threadId4 = UUID.randomUUID();
    UUID threadId5 = UUID.randomUUID();

    List<String> commentList = new ArrayList<>();
    List<String> commentList2 = new ArrayList<>();
    List<String> commentList3 = new ArrayList<>();
    List<String> commentList4 = new ArrayList<>();
    List<String> commentList5 = new ArrayList<>();

//    for (int i = 0; i < 10000; i++) {
//      commentList.add("이 정책은 정말 좋은 것 같습니다." + i);
//      commentList2.add("이 정책은 정말 좋은 것 같습니다." + i);
//      commentList3.add("이 정책은 정말 좋은 것 같습니다." + i);
//      commentList4.add("이 정책은 정말 좋은 것 같습니다." + i);
//      commentList5.add("이 정책은 정말 좋은 것 같습니다." + i);
//    }

    for (int i = 0; i < 100; i++) {

      commentList.add("이 정책은 좋은 것 같아" + i);
      commentList.add("이 정책은 정말 별로야" + i);
      commentList.add("좋은지 나쁜지 잘 모르겠어" + i);
      commentList.add("아 배고프다." + i);
    }

    // when
    String result = geminiClient.analyzeSummary(threadId, commentList);
//    String result1 = geminiClient.analyzeSummary(threadId2, commentList2);
//    String result2 = geminiClient.analyzeSummary(threadId3, commentList3);
//    String result3 = geminiClient.analyzeSummary(threadId4, commentList4);
//    String result4 = geminiClient.analyzeSummary(threadId5, commentList5);

    // then
    assertNotNull(result);
    System.out.println("[Gemini 요약 결과 0] " + result);
//    System.out.println("[Gemini 요약 결과 1] " + result1);
//    System.out.println("[Gemini 요약 결과 2] " + result2);
//    System.out.println("[Gemini 요약 결과 3] " + result3);
//    System.out.println("[Gemini 요약 결과 4] " + result4);

//    [Gemini 요약 결과 0] *   **긍정:** 25%
//        *   **부정:** 25%
//        *   **중립:** 25%
//        *   **무관심:** 25%
  }

}
