package com.newpick4u.newsorigin.newsorigin.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.newpick4u.newsorigin.newsorigin.application.OriginCollectClient;
import com.newpick4u.newsorigin.newsorigin.application.dto.NewNewsOriginDto;
import com.newpick4u.newsorigin.newsorigin.domain.entity.NewsOrigin;
import com.newpick4u.newsorigin.newsorigin.infrastructure.jpa.NewsOriginJpaRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NewsOriginServiceImpl implements NewsOriginService {

  private final OriginCollectClient originCollectClient;
  private final NewsOriginJpaRepository newsOriginRepository;

  // 뉴스 수집 및 저장
  public int collectOriginNews() {
    int count = 0;
    try {
      ArrayList<NewNewsOriginDto> originNewsList = originCollectClient.getOriginNewsList();

      List<NewsOrigin> newsOriginList = originNewsList.stream()
          .map(dto -> NewsOrigin.of(dto.publishedDate(), dto.url()))
          .toList();

      // TODO : 중복 기사 제거 로직 추가 예정

      List<NewsOrigin> savedNewsOriginList = newsOriginRepository.saveAll(newsOriginList);
      count = savedNewsOriginList.size();
    } catch (JsonProcessingException e) {
      log.error("", e);
    }
    return count;
  }

  // TODO : 뉴스 조회 및 전송
  
}
