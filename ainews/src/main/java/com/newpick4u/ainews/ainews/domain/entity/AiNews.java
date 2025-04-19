package com.newpick4u.ainews.ainews.domain.entity;

import com.newpick4u.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_ai_news")
@Entity
public class AiNews extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "ai_news_id")
  private UUID id;

  @Column(length = 255, nullable = false, unique = true)
  private UUID originNewsId;

  @Column(length = 255, nullable = false)
  private String url;

  @Column(length = 255)
  private String title;

  @Column(length = 255)
  private String keywords;

  @Column(columnDefinition = "TEXT")
  private String summary;

  @Column(length = 100)
  private String publishedDate;

  @Column(columnDefinition = "TEXT")
  private String originalResponse;

  @Builder
  private AiNews(UUID originNewsId, String url, String title, String keywords, String summary,
      String publishedDate,
      String originalResponse) {
    this.originNewsId = originNewsId;
    this.url = url;
    this.title = title;
    this.keywords = keywords;
    this.summary = summary;
    this.publishedDate = publishedDate;
    this.originalResponse = originalResponse;
  }

  public static AiNews create(
      UUID originNewsId,
      String url,
      String title,
      String keywords,
      String summary,
      String publishedDate,
      String originalResponse
  ) {
    return AiNews.builder()
        .originNewsId(originNewsId)
        .url(url)
        .title(title)
        .keywords(keywords)
        .summary(summary)
        .publishedDate(publishedDate)
        .originalResponse(originalResponse)
        .build();
  }

  public List<String> getTagList() {
    String[] tags = this.keywords
        .replace("[", "")
        .replace("]", "")
        .split(",");
    return Arrays.asList(tags);
  }
}
