package com.newpick4u.news.news.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "news_id")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String aiNewsId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String publishedDate;

    @Enumerated(EnumType.STRING)
    @Column
    private NewsStatus status;

    @Column
    private Long view;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<NewsTag> newsTagList;

    @Builder(access = AccessLevel.PRIVATE)
    private News(String aiNewsId, String title, String content, String url, String publishedDate, Long view, NewsStatus status) {
        this.aiNewsId = aiNewsId;
        this.title = title;
        this.content = content;
        this.url = url;
        this.publishedDate = publishedDate;
        this.view = view;
        this.status = status;
    }

    public static News create(String aiNewsId, String title, String content, String url, String publishedDate, Long view) {
        return News.builder()
                .aiNewsId(aiNewsId)
                .title(title)
                .content(content)
                .view(view)
                .url(url)
                .publishedDate(publishedDate)
                .status(NewsStatus.PENDING)
                .build();
    }

    public void addTags(List<NewsTag> newTags) {
        this.newsTagList.addAll(newTags);
        this.status = NewsStatus.ACTIVE;
    }

}
