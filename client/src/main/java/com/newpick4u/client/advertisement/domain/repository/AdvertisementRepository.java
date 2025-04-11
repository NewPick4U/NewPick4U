package com.newpick4u.client.advertisement.domain.repository;

import com.newpick4u.client.advertisement.domain.entity.Advertisement;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository {

  Advertisement save(Advertisement advertisement);

  boolean existsByTitleOrUrl(String title, String url);

  Optional<Advertisement> findById(UUID advertisementId);
}
