package com.newpick4u.newsorigin.newsorigin.infrastructure.jpa;

import com.newpick4u.newsorigin.newsorigin.domain.entity.NewsOrigin;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsOriginJpaRepository extends JpaRepository<NewsOrigin, UUID> {

}
