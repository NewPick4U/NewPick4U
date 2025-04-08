package com.newpick4u.client.advertisement.application.usecase;

import com.newpick4u.client.advertisement.application.client.NewsClient;
import com.newpick4u.client.advertisement.application.dto.request.CreateAdvertiseRequestDto;
import com.newpick4u.client.advertisement.application.dto.response.GetNewsResponseDto;
import com.newpick4u.client.advertisement.application.exception.AdvertisementException;
import com.newpick4u.client.advertisement.domain.entity.Advertisement;
import com.newpick4u.client.advertisement.domain.repository.AdvertisementRepository;
import com.newpick4u.common.response.ApiResponse;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

  private final AdvertisementRepository advertisementRepository;
  private final NewsClient newsClient;

  @Transactional
  public UUID createAdvertisement(CreateAdvertiseRequestDto request) {
    // TODO 뉴스 도메인의 객체 반환방식에 따라 변돋이 생길 수 있음
    UUID newsId = getNews(request);
    if (validateCondition(request.title(), request.url())) {
      throw new AdvertisementException.AlreadyExistsTitleOrUrl();
    }
    Advertisement advertisement = Advertisement.create(request.clientId(), request.newsId(),
        request.title(), request.content(),
        request.type(), request.url(), request.budget());
    Advertisement saveAdvertisement = advertisementRepository.save(advertisement);
    return saveAdvertisement.getAdvertisementId();
  }


  private UUID getNews(CreateAdvertiseRequestDto requestDto) {
    ResponseEntity<ApiResponse<GetNewsResponseDto>> response = newsClient.getNews(
        requestDto.newsId());
    if (Objects.nonNull(response) && response.getStatusCode().is2xxSuccessful()) {
      UUID newsId = Objects.requireNonNull(response.getBody()).data().getId();
      return newsId;
    }
    throw new AdvertisementException.NewsNotFoundException();
  }

  private boolean validateCondition(String title, String url) {
    return advertisementRepository.existsByTitleOrUrl(title, url);
  }

}
