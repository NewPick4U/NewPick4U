package com.newpick4u.news.news.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_news_tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewsTag {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "news_tag_id", nullable = false, unique = true)
    private UUID newsTagId;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false, unique = true)
    private News news;

    public static NewsTag create(String name, News news) {
        return new NewsTag(null,name,news);
    }
}
