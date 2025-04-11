package com.newpick4u.tag.domain.repository;

import com.newpick4u.tag.domain.criteria.SearchTagCriteria;
import com.newpick4u.tag.domain.entity.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagRepository {

  Page<Tag> searchByCriteria(SearchTagCriteria criteria, Pageable pageable);

  Optional<Tag> findById(UUID id);

  Tag save(Tag tag);

  Optional<Tag> findByTagName(String tagName);

  void delete(Tag tag);

  List<Tag> findAll();

  void deleteAll();
}