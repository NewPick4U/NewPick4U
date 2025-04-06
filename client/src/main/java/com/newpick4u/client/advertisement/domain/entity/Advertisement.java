package com.newpick4u.client.advertisement.domain.entity;

import com.newpick4u.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "p_advertisement")
public class Advertisement extends BaseEntity {

  @Id
  @UuidGenerator
  @Column(name = "advertisement_id", nullable = false)
  private UUID advertisementId;
  @Column(name = "client_id", nullable = false)
  private UUID clientId;
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

  @Builder
  private Advertisement(UUID clientId, String title, String content,
      AdvertisementType type, String url, Long budget) {
    this.clientId = clientId;
    this.title = title;
    this.content = content;
    this.type = type;
    this.url = url;
    this.budget = budget;
  }


  public static Advertisement create(UUID clientId, String title, String content,
      AdvertisementType type, String url, Long budget) {
    return Advertisement.builder()
        .clientId(clientId)
        .title(title)
        .content(content)
        .type(type)
        .url(url)
        .budget(budget)
        .build();
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
