package com.newpick4u.newsorigin.newsorigin.domain.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_news_origin")
@Entity
public class NewsOrigin extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "news_origin_id")
  private UUID id;

  @Column(length = 255, unique = true, nullable = false)
  private String url;

  @Column(length = 255)
  private String title;

  @Column(length = 50)
  @Enumerated(EnumType.STRING)
  private Status status;

  private LocalDateTime newsPublishedDate;

  private NewsOrigin(String title, String url, LocalDateTime newsPublishedDate) {
    this.title = title;
    this.url = url;
    this.newsPublishedDate = newsPublishedDate;
    this.status = Status.PENDING;
  }

  public static NewsOrigin create(String title, String url, LocalDateTime newsPublishedDate) {
    return new NewsOrigin(title, url, newsPublishedDate);
  }

  public void sentToQueue() {
    this.status = Status.SENDED;
  }

  public void sendFail() {
    this.status = Status.FAIL;
  }

  public enum Status {
    PENDING,
    SENDED,
    FAIL
  }
}
