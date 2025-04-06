package com.newpick4u.client.survey.domain.entity;

import com.newpick4u.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "p_survey")
public class Survey extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "survey_id", nullable = false)
  private UUID surveyId;
  @Column(name = "advertisement_id", nullable = false)
  private UUID advertisementId;
  @Column(nullable = false, length = 100)
  private String title;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SurveyType type;
  @Column(nullable = false)
  private Integer numQuestions;
  @Column(nullable = false)
  private LocalDateTime startDate;
  @Column(nullable = false)
  private LocalDateTime endDate;

  @Builder
  private Survey(UUID advertisementId, String title, SurveyType type,
      Integer numQuestions, LocalDateTime startDate, LocalDateTime endDate) {
    this.advertisementId = advertisementId;
    this.title = title;
    this.type = type;
    this.numQuestions = numQuestions;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public static Survey create(UUID advertisementId, String title, SurveyType type,
      Integer numQuestions, LocalDateTime startDate, LocalDateTime endDate) {
    return Survey.builder()
        .advertisementId(advertisementId)
        .title(title)
        .type(type)
        .numQuestions(numQuestions)
        .startDate(startDate)
        .endDate(endDate)
        .build();
  }

  @Getter
  public enum SurveyType {
    CUSTOMER_SATISFACTION("고객 만족도 조사"),
    PRODUCT_IMPROVEMENT("제품 개선 제안 조사"),
    BRAND_AWARENESS("브랜드 인지도 조사"),
    CONSUMER_BEHAVIOR("소비자 행동 조사");

    private final String description;

    SurveyType(String description) {
      this.description = description;
    }

  }


}
