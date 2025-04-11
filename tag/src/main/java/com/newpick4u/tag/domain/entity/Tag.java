package com.newpick4u.tag.domain.entity;

import com.newpick4u.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "p_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "tag_id", columnDefinition = "CHAR(36)")
  private UUID tagId;

  @Column(nullable = false, unique = true)
  private String tagName;

  @ColumnDefault("1")
  private Long score;

  private Tag(String tagName, Long score) {
    this.tagName = tagName;
    this.score = score;
  }

  public static Tag create(String tagName) {
    return new Tag(tagName, 1L);
  }

  public void updateTagName(String newTagName) {
    if (newTagName != null && !newTagName.isBlank()) {
      this.tagName = newTagName;
    }
  }

  public void increaseScore() {
    this.score++;
  }

  public void decreaseScore() {
    this.score--;
  }
}
