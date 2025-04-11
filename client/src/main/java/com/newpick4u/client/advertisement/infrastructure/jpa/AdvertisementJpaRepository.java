package com.newpick4u.client.advertisement.infrastructure.jpa;

import com.newpick4u.client.advertisement.domain.entity.Advertisement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementJpaRepository extends JpaRepository<Advertisement, UUID> {

  boolean existsByTitleOrUrl(String title, String url);
}
