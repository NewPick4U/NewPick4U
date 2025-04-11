package com.newpick4u.client.advertisement.domain.entity;

import com.newpick4u.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "p_advertisement")
public class Advertisement extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "advertisement_id", nullable = false)
  private UUID advertisementId;
  @Column(name = "client_id", nullable = false)
  private UUID clientId;
  @Column(name = "news_id", nullable = false)
  private UUID newsId;
  @Column(nullable = false, length = 50)
  private String title;
  @Column(nullable = false)
  private String content;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AdvertisementType type;
  @Column(nullable = false)
  private String url;
  @Column(nullable = false)
  private Long budget;
  @Column(nullable = false)
  private Integer pointGrantCount;
  @Column(nullable = false)
  private boolean isPointGrantFinished;
  @Column(nullable = false)
  private Integer maxPointGrantCount;

  @Builder
  private Advertisement(UUID clientId, UUID newsId, String title, String content,
      AdvertisementType type, String url, Long budget, Integer maxPointGrantCount) {
    this.clientId = clientId;
    this.newsId = newsId;
    this.title = title;
    this.content = content;
    this.type = type;
    this.url = url;
    this.budget = budget;
    this.maxPointGrantCount = maxPointGrantCount;
    this.pointGrantCount = 0;
    this.isPointGrantFinished = false;
  }


  public static Advertisement create(UUID clientId, UUID newsId, String title, String content,
      AdvertisementType type, String url, Long budget, Integer maxPointGrantCount) {
    return Advertisement.builder()
        .clientId(clientId)
        .newsId(newsId)
        .title(title)
        .content(content)
        .type(type)
        .url(url)
        .budget(budget)
        .maxPointGrantCount(maxPointGrantCount)
        .build();
  }

  public void incrementPointGrantCount() {
    this.pointGrantCount++;
  }

  public void updateIsPointGrantFinished() {
    this.isPointGrantFinished = true;
  }

  public boolean isMaxPointGrantCountEqualToCurrentPointGrantCount() {
    return Objects.equals(this.maxPointGrantCount, this.pointGrantCount);
  }

  @Getter
  public enum AdvertisementType {
    BANNER("배너 광고"),
    VIDEO("비디오 광고"),
    POPUP("팝업 광고"),
    NATIVE("네이티브 광고");

    private final String description;

    AdvertisementType(String description) {
      this.description = description;
    }
  }


}
