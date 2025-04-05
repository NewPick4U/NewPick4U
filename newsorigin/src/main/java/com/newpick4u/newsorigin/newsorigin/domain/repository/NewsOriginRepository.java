package com.newpick4u.newsorigin.newsorigin.domain.repository;

import com.newpick4u.newsorigin.newsorigin.domain.entity.NewsOrigin;
import java.util.List;

public interface NewsOriginRepository {

  List<NewsOrigin> saveAll(List<NewsOrigin> newsOriginList);
}
