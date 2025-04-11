package com.newpick4u.tag.infrastructure.jpa;

import com.newpick4u.tag.domain.entity.Tag;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagJpaRepository extends JpaRepository<Tag, UUID> {

  Optional<Tag> findByTagName(String tagName);
}
