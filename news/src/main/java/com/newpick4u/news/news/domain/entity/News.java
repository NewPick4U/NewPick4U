package com.newpick4u.news.news.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_news")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class News {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "news_id", nullable = false, unique = true)
    private UUID newsId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private Long view;

    @OneToMany(mappedBy = "p_news", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<NewsTag> tagList = new ArrayList<>();

    public static News create(String title, String content, Long view) {
        return new News(null, title, content, view, new ArrayList<>());
    }


}
