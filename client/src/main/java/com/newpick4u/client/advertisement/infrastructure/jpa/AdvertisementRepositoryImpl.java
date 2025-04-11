package com.newpick4u.client.advertisement.infrastructure.jpa;

import com.newpick4u.client.advertisement.domain.entity.Advertisement;
import com.newpick4u.client.advertisement.domain.repository.AdvertisementRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdvertisementRepositoryImpl implements AdvertisementRepository {

  private final AdvertisementJpaRepository jpaAdvertisementRepository;

  @Override
  public Advertisement save(Advertisement advertisement) {
    return jpaAdvertisementRepository.save(advertisement);
  }

  @Override
  public boolean existsByTitleOrUrl(String title, String url) {
    return jpaAdvertisementRepository.existsByTitleOrUrl(title, url);
  }

  @Override
  public Optional<Advertisement> findById(UUID advertisementId) {
    return jpaAdvertisementRepository.findById(advertisementId);
  }
}
