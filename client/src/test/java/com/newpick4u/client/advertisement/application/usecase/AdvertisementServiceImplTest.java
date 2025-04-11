package com.newpick4u.client.advertisement.application.usecase;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.newpick4u.client.advertisement.application.dto.request.CreateAdvertiseRequestDto;
import com.newpick4u.client.advertisement.application.dto.response.GetNewsResponseDto;
import com.newpick4u.client.advertisement.application.exception.AdvertisementException;
import com.newpick4u.client.advertisement.domain.entity.Advertisement;
import com.newpick4u.client.advertisement.domain.entity.Advertisement.AdvertisementType;
import com.newpick4u.client.advertisement.domain.repository.AdvertisementRepository;
import com.newpick4u.client.advertisement.infrastructure.client.NewsClientImpl;
import com.newpick4u.common.response.ApiResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceImplTest {

  @InjectMocks
  private AdvertisementServiceImpl advertisementService;
  @Mock
  private AdvertisementRepository advertisementRepository;
  @Mock
  private NewsClientImpl newsClient;

  @Test
  @DisplayName("광고 생성 테스트 - 성공 케이스")
  void createAdvertisement() {

    // given
    CreateAdvertiseRequestDto request = new CreateAdvertiseRequestDto(
        UUID.randomUUID(), UUID.randomUUID(), "스파르타 코딩클럽 모집", "스파르타1111",
        AdvertisementType.BANNER, "www.ddd.ttt", 50000000L, 30); // 최대 지급 횟수 30 설정

    Advertisement advertisement = Advertisement.create(request.clientId(), request.newsId(),
        request.title(),
        request.content(), request.type(), request.url(), request.budget(),
        request.maxPointGrantCount());

    ReflectionTestUtils.setField(advertisement, "advertisementId", UUID.randomUUID());
    ReflectionTestUtils.setField(advertisement, "maxPointGrantCount",
        request.maxPointGrantCount()); // 최대 지급 횟수 설정

    GetNewsResponseDto getNewsResponseDto = new GetNewsResponseDto(request.newsId());
    ResponseEntity<ApiResponse<GetNewsResponseDto>> getNewsResponseEntity = new ResponseEntity<>(
        ApiResponse.of(HttpStatus.OK, getNewsResponseDto), HttpStatus.OK);

    when(newsClient.getNews(request.newsId())).thenReturn(getNewsResponseEntity);
    when(advertisementRepository.save(any(Advertisement.class))).thenReturn(advertisement);

    // then
    UUID savedAdvertisementId = advertisementService.createAdvertisement(request);

    assertNotNull(savedAdvertisementId);
    assertEquals(advertisement.getAdvertisementId(), savedAdvertisementId);
  }

  @Test
  @DisplayName("광고 생성 테스트 - 실패 케이스(뉴스 정보 부재)")
  void createAdvertisement_withNotFoundNews() {

    // given
    CreateAdvertiseRequestDto request = new CreateAdvertiseRequestDto(
        UUID.randomUUID(), UUID.randomUUID(), "스파르타 코딩클럽 모집", "스파르타1111",
        AdvertisementType.BANNER, "www.ddd.ttt", 50000000L, 30); // 최대 지급 횟수 30 설정

    when(newsClient.getNews(request.newsId())).thenThrow(
        AdvertisementException.NewsNotFoundException.class);

    // when & then
    assertThatThrownBy(() -> advertisementService.createAdvertisement(request))
        .isInstanceOf(AdvertisementException.NewsNotFoundException.class);
  }

  @Test
  @DisplayName("광고 생성 테스트 - 실패 케이스(광고명 or 광고url 중복)")
  void createAdvertisement_withDuplicateTitleOrUrl() {

    // given
    CreateAdvertiseRequestDto request = new CreateAdvertiseRequestDto(
        UUID.randomUUID(), UUID.randomUUID(), "스파르타 코딩클럽 모집", "스파르타1111",
        AdvertisementType.BANNER, "www.ddd.ttt", 50000000L, 30); // 최대 지급 횟수 30 설정

    GetNewsResponseDto getNewsResponseDto = new GetNewsResponseDto(request.newsId());
    ResponseEntity<ApiResponse<GetNewsResponseDto>> getNewsResponseEntity = new ResponseEntity<>(
        ApiResponse.of(HttpStatus.OK, getNewsResponseDto), HttpStatus.OK);

    when(newsClient.getNews(request.newsId())).thenReturn(getNewsResponseEntity);
    when(advertisementRepository.existsByTitleOrUrl(request.title(), request.url()))
        .thenThrow(AdvertisementException.AlreadyExistsTitleOrUrlException.class);

    // when & then
    assertThatThrownBy(() -> advertisementService.createAdvertisement(request))
        .isInstanceOf(AdvertisementException.AlreadyExistsTitleOrUrlException.class);
  }
}
