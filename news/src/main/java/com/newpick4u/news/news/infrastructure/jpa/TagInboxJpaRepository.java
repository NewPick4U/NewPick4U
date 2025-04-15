package com.newpick4u.news.news.infrastructure.jpa;

import com.newpick4u.news.news.domain.entity.TagInbox;
import com.newpick4u.news.news.domain.repository.TagInboxRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TagInboxJpaRepository extends JpaRepository<TagInbox, UUID> {
}
