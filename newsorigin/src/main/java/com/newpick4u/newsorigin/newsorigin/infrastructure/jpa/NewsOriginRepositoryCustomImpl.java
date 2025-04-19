package com.newpick4u.newsorigin.newsorigin.infrastructure.jpa;

import static com.newpick4u.newsorigin.newsorigin.domain.entity.QNewsOrigin.newsOrigin;

import com.newpick4u.newsorigin.newsorigin.domain.entity.NewsOrigin;
import com.newpick4u.newsorigin.newsorigin.domain.entity.NewsOrigin.Status;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NewsOriginRepositoryCustomImpl implements NewsOriginRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<NewsOrigin> getAllByBeforeSentQueue() {
    return queryFactory.selectFrom(newsOrigin)
        .where(
            newsOrigin.status.eq(Status.PENDING)
                .and(newsOrigin.deletedAt.isNull())
        )
        .fetch();
  }
}
