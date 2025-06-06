package com.newpick4u.comment.comment.domain.entity;

import com.newpick4u.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_comment")
@Entity
public class Comment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "comment_id")
  private UUID id;

  private UUID newsId;

  private UUID threadId;

  @Column(columnDefinition = "TEXT")
  private String content;

  private Long goodCount = 0L;

  @Column(length = 50)
  private String username;

  @OneToMany(mappedBy = "comment",
      cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
  private List<CommentGood> goodList = new ArrayList<>();

  @Builder
  private Comment(UUID newsId, UUID threadId, String content, String username) {
    this.newsId = newsId;
    this.threadId = threadId;
    this.content = content;
    this.goodCount = 0L;
    this.goodList = new ArrayList<>();
    this.username = username;
  }

  public static Comment createForNews(UUID newsId, String content, String username) {
    return Comment.builder()
        .newsId(newsId)
        .threadId(null)
        .content(content)
        .username(username)
        .build();
  }

  public static Comment createForThread(UUID threadId, String content, String username) {
    return Comment.builder()
        .newsId(null)
        .threadId(threadId)
        .content(content)
        .username(username)
        .build();
  }

  public void updateContent(String content) {
    this.content = content;
  }

  public Long addGood(Long userId) {
    CommentGood commentGood = CommentGood.create(this, userId);
    this.goodList.add(commentGood);
    this.goodCount++;
    return this.goodCount;
  }

  public Long deleteGood(CommentGood commentGood) {
    if (this.goodList.remove(commentGood)) {
      this.goodCount--;
    }
    return this.goodCount;
  }
}
