package com.newpick4u.newsorigin.newsorigin.infrastructure.jpa;

import com.newpick4u.newsorigin.newsorigin.domain.entity.NewsOrigin;
import com.newpick4u.newsorigin.newsorigin.domain.repository.NewsOriginRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NewsOriginRepositoryImpl implements NewsOriginRepository {

  private final NewsOriginJpaRepository newsOriginJpaRepository;
  private final NewsOriginRepositoryCustom newsOriginRepositoryCustom;

  public List<NewsOrigin> saveAll(List<NewsOrigin> newsOriginList) {
    return newsOriginJpaRepository.saveAll(newsOriginList);
  }
}
