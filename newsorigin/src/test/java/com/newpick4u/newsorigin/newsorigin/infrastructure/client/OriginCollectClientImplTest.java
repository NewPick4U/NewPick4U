package com.newpick4u.newsorigin.newsorigin.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class OriginCollectClientImplTest {

  @Autowired
  OriginCollectClientImpl originCollectClient;

  @Test
  @DisplayName("api 정상 호출 테스트")
  void getNewsTest() {
//    Assertions.assertDoesNotThrow(
//        () -> originCollectClient.getOriginNewsList()
//    );
  }
}