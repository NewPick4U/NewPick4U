package com.newpick4u.tag.infrastructure.jpa;

import com.newpick4u.tag.domain.criteria.SearchTagCriteria;
import com.newpick4u.tag.domain.entity.Tag;
import com.newpick4u.tag.domain.repository.TagRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TagRepositoryImpl implements TagRepository {

  private final TagJpaRepository tagJpaRepository;
  private final TagJpaRepositoryCustom tagJpaRepositoryCustom;

  @Override
  public Page<Tag> searchByCriteria(SearchTagCriteria criteria, Pageable pageable) {
    return tagJpaRepositoryCustom.searchByCriteria(criteria, pageable);
  }

  @Override
  public Optional<Tag> findById(UUID id) {
    return tagJpaRepository.findById(id);
  }

  @Override
  public Tag save(Tag tag) {
    return tagJpaRepository.save(tag);
  }

  @Override
  public Optional<Tag> findByTagName(String tagName) {
    return tagJpaRepository.findByTagName(tagName);
  }

  @Override
  public void delete(Tag tag) {
    tagJpaRepository.delete(tag);
  }

  @Override
  public List<Tag> findAll() {
    return tagJpaRepository.findAll();
  }

  @Override
  public void deleteAll() {
    tagJpaRepository.deleteAll();
  }
}
