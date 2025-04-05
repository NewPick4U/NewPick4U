package com.newpick4u.newsorigin.newsorigin.infrastructure.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NewsOriginRepositoryCustomImpl implements NewsOriginRepositoryCustom {

  private final JPAQueryFactory queryFactory;

}
