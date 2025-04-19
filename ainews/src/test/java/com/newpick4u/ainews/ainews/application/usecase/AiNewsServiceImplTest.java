package com.newpick4u.ainews.ainews.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.ainews.ainews.application.dto.NewsOriginDto;
import com.newpick4u.ainews.ainews.infrastructure.jpa.AiNewsJpaRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
//@EmbeddedKafka(
//    partitions = 3,
//    ports = {9092},
//    brokerProperties = {
//        "listeners=PLAINTEXT://localhost:9092"
//    },
//    topics = {
//        "${app.kafka.producer.normal.topic.news-info.topic-name}",
//        "${app.kafka.producer.normal.topic.tag-info.topic-name}",
//
//        "${app.kafka.producer.exceptional.topic.originnews-info-dlq.topic-name}",
//        "${app.kafka.producer.exceptional.topic.ainews-dlq.topic-name}",
//        "${app.kafka.producer.exceptional.topic.news-info-dlq.topic-name}",
//        "${app.kafka.producer.exceptional.topic.tag-info-dlq.topic-name}",
//    })
@MockitoBean(types = ScheduledAnnotationBeanPostProcessor.class) // @Scheduled 무력화
@ActiveProfiles("test")
@SpringBootTest
class AiNewsServiceImplTest {

  @Autowired
  AiNewsServiceImpl aiNewsService;

  @Autowired
  AiNewsJpaRepository jpaRepository;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  @DisplayName("정상 흐름 테스트")
  void allClearTest() throws JsonProcessingException {
    // 불필요한 호출을 막기위한 주석처리
//    // given
//    NewsOriginDto newsOriginDto = new NewsOriginDto(
//        "00000000-0000-0000-0000-000000000001",
//        "‘벚꽃 성곽’ 품은 동네…동래로 봄마실",
//        "https:\\/\\/n.news.naver.com\\/mnews\\/article\\/025\\/0003433445?sid=103",
//        "2025-04-11T00:25",
//        """
//            부산은 산이다. 이름부터 부산은 산이다. 부산(釜山). 가마솥을 닮은 산이 있는 고장이란 뜻이다. 그 산이 부산의 진산 금정산(801m)이다. 금정산 자락을 따라 부락이 들어섰고, 그 산자락 부락이 피란민이 내려오기 전의 부산을 이뤘다.
//
//            부산의 원적과 같은 마을이 동래다. 지금은 부산시 동래구만 동래로 불리지만, 원래는 금정산 아랫도리를 두른 기장군·금정구·연제구 모두 동래였다. 동래는 부산의 시작이었다. 부산 관광도 동래에서 비롯됐다. 숱한 전설과 신화를 거느린 천 년 온천 덕분이다. 요즘은 예전 같은 영화를 누리지 못한다지만, 동래온천 물은 여전히 좋았다.
//
//            온천천에 벚꽃이 막 피기 시작한 4월 첫 주말 동래로 봄마실을 다녀왔다. 아련한 옛 추억에 잠겨볼까 했었는데 저런, 동래는 짐작보다 더 활달하고 발랄했다.
//
//            뚜껑 덮어 찌듯이 지져내는 ‘할매파전’
//            동래읍성부터 올랐다. 조선의 북쪽 국경 도시가 압록강 아래 의주성이었다면 남쪽 국경 도시는 부산포 위 동래성이었다. 여기서 임진왜란의 비극이 시작됐다. 동래부사 송상현(1551∼92)이 고니시 유키나가(小西行長)가 이끄는 왜군 선봉대에 맞서 싸우다 이곳에서 순절했다. 그때 송상현이 남긴 문장이 유명하다. ‘싸워서 죽기는 쉬우나 길을 빌려주기는 어렵다.’
//
//            신재민 기자
//            신재민 기자
//            비정한 역사를 아는지 모르는지 봄날의 동래읍성은 화사했다. 성터 주변이 사적공원으로 꾸며져 부산 시민은 물론이고 관광객의 발길이 줄을 이었다. 마침 벚꽃 핀 날이었다. 젊은 연인부터 웨딩 촬영하는 신혼부부까지 벚꽃 드리운 성곽길에서 봄을 만끽했다.
//
//            읍성 아래에 동래시장이 자리한다. 1770년 개장한 유서 깊은 전통시장이다. 지금 동래시장은 먹자골목에서 파는 메밀칼국수로 유명하다. 각종 SNS에 동래시장 메밀칼국수 인증 사진이 올라온다. SNS 명소로 뜬 집은 따로 있지만, 시장에서 칼국수 삶는 집이 여남은 곳이다. 가격도 똑같고 맛도 비슷하다.
//
//            ‘동래할매파전’의 4대 대표 김정희씨.
//            ‘동래할매파전’의 4대 대표 김정희씨.
//            이 시장에서 동래 별미 ‘동래파전’이 탄생했다. 예부터 금정산 자락은 파밭이 많았단다. 풋풋한 조선 쪽파 넣고 부산포 앞바다에서 나오는 해물 얹어 무쇠 번철에 파전을 부쳐 먹었단다. 동래부사가 임금에 파전을 진상했다는 주장도 있다. 아무튼, 1930년대 동래 할매들이 동래시장에서 좌판 깔고 파전을 부쳐 팔았는데, 그중 한 할매네 집이 아직도 파전을 부친다. 이름하여 ‘동래할매파전’. 대한민국 ‘백년 명가’를 꼽을 때 한 번도 빠지지 않은 전설의 노포다.
//
//            동래할매파전은 동래구청 바로 옆에서 오늘도 파전을 부친다. 1대 강매희, 2대 이윤선, 3대 김옥자 대표에 이어 지금은 4대 김정희(61) 대표가 전집을 지킨다. 4대 모두 며느리가 대물림했다.
//
//            동래할매파전은 여느 파전과 다르다. 바삭하게 굽지 않고 냄비 뚜껑을 덮어 찌듯이 지진다. 하여 일본 빈대떡 오코노미야키처럼 속이 부드럽다. 4대 대표에게 지난 세월을 물었다.
//
//            “파전을 지켜오는 게 쉽지는 않았어요. 파전은 곁들여 먹는 음식이잖아요. 다른 음식은 개발했어도 파전만큼은 옛날 방식 그대로 부치고 있습니다. 요즘이 쪽파가 제일 맛있을 때예요. 옛날에도 삼짇날 즈음해서 파전을 부쳐 먹었대요.”
//
//            이창호 ‘상하이 대첩’ 시작된 농심호텔
//            농심호텔에 딸린 초대형 온천탕 ‘허심청’.
//            농심호텔에 딸린 초대형 온천탕 ‘허심청’.
//            팔도의 수다한 온천 중에서 동래는 가장 유장한 이력을 자랑한다. 무려 『삼국유사』에 등장한다. 국내 온천에 관한 첫 공식 기록의 주인공이 동래 온천이다. 고려와 조선을 거치며 동래 온천은 팔도를 주름잡았고, 구한말 온천 좋아하는 일본인에 의해 근대식 온천 관광지로 개발됐다.
//
//            1907년 일본인은 동래에 온천장 ‘봉래관’을 개관했다. 그 봉래관이 지금의 농심호텔이다. 농심호텔에 딸린 온천탕이 ‘허심청’이다. 한때 동양 최대 목욕탕으로 불렸던 그곳이다. 허심청은 여전히 크다. 동시 수용인원이 남탕·여탕 합쳐 4000명이다.
//
//            동래의 옛 전차 종점 조형물.
//            동래의 옛 전차 종점 조형물.
//            동래는 자체로 온천 마을이다. 마을 앞 지하철역 이름도 온천장역이고, 마을을 가로지르는 개천 이름도 온천천이다. 동래 온천은 약알칼리성 온천이다. 물이 매끄럽고 냄새도 없다. 수온은 56도다. 일제 강점기에는 동래 온천 바로 앞까지 전차가 다녔었다. 그 흔적이 마을 곳곳에 남아있다.
//
//            농심호텔과 허심청을 연결하는 구름다리. 이창호 9단의 ‘상하이 대첩’을 기념하는 포토존이 최근 설치됐다.
//            농심호텔과 허심청을 연결하는 구름다리. 이창호 9단의 ‘상하이 대첩’을 기념하는 포토존이 최근 설치됐다.
//            농심호텔은 사실 한국 바둑의 숨은 성지다. 이창호 9단의 소위 ‘상하이 대첩’이 시작된 현장이다. 농심은 올해로 26년째 한·중·일 국가대항전 ‘농심 신라면배 세계바둑최강전’을 후원하고 있다. 상하이 대첩의 첫 장이 2004년 11월 29일 제6회 농심배 2라운드 최종국이 열린 농심호텔에서 쓰였다. 한국팀의 마지막 선수 이창호가 이날 중국 선수를 꺾었고, 이듬해 2월 중국 상하이로 넘어가 중국과 일본 선수 4명을 다 무찔러 한국의 대역전 드라마를 완성했다.
//
//            그때 사진이 전해온다. 이창호 홀로 대국장으로 걸어가는 장면을 포착한 사진이다. 사진 속 통로가 허심청과 농심호텔을 연결한 구름다리다. 농심호텔이 최근 통로 입구에 포토존을 설치했다.
//
//            ‘모모스 커피’ 온천장 본점의 시그니처 메뉴 ‘모두의 정원’.
//            ‘모모스 커피’ 온천장 본점의 시그니처 메뉴 ‘모두의 정원’.
//            천 년 온천 마을에도 젊은이가 찾아든다. 그 증거가 온천장역 건너편의 ‘모모스 커피’다. 2019년 미국에서 열린 ‘월드 바리스타 챔피언십’에서 한국인 최초로 우승한 전주연(38)씨가 이곳에서 일한다. 전국구 명소여서 온종일 빈자리가 거의 없다. ‘에티오피아 예가체프 단체’ 원두를 쓴 ‘오늘의 커피’가 기억에 남는다. 커피가 식었는데도 향과 맛이 가시지 않았다.
//            """
//    );
//    String inputMessage = objectMapper.writeValueAsString(newsOriginDto);
//
//    // when
//    aiNewsService.processAiNews(inputMessage);
//    AiNews aiNews = jpaRepository.findByOriginNewsId(UUID.fromString(newsOriginDto.originNewsId()))
//        .get();
//
//    Assertions.assertEquals(
//        aiNews.getOriginNewsId(),
//        UUID.fromString(newsOriginDto.originNewsId()));
  }

  @Test
  @DisplayName("연속 로직 호출 테스트")
  void apiCallTest() throws IOException {
//    List<NewsOriginDto> messageDtos = readAllConsumerMessages();
//    List<String> consumeMessages = new ArrayList<>();
//    for (int i = 0; i < 100; i++) {
//      NewsOriginDto newsOriginDto = messageDtos.get(i % messageDtos.size());
//      String String = objectMapper.writeValueAsString(
//          new NewsOriginDto(UUID.randomUUID().toString(), newsOriginDto.title(),
//              newsOriginDto.url(),
//              newsOriginDto.publishedDate(), newsOriginDto.body()));
//      consumeMessages.add(String);
//    }
//    for (String consumeMessage : consumeMessages) {
//      aiNewsService.processAiNews(consumeMessage);
//    }
  }

  public List<NewsOriginDto> readAllConsumerMessages() throws IOException {
    List<NewsOriginDto> messages = new ArrayList<>();

    Path resourceDir = Paths.get("src/test/resources/consumer-message");
    try (Stream<Path> pathStream = Files.walk(resourceDir)) {
      pathStream
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".json"))
          .sorted()
          .forEach(path -> {
            try {
              String content = Files.readString(path, StandardCharsets.UTF_8);
              NewsOriginDto newsOriginDto = objectMapper.readValue(content, NewsOriginDto.class);
              messages.add(newsOriginDto);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    }

    return messages;
  }
}