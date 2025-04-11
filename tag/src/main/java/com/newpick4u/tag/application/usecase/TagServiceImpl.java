package com.newpick4u.tag.application.usecase;

import com.newpick4u.common.exception.CustomException;
import com.newpick4u.common.exception.type.ApiErrorCode;
import com.newpick4u.tag.application.dto.AiNewsDto;
import com.newpick4u.tag.application.dto.UpdateTagRequestDto;
import com.newpick4u.tag.domain.criteria.SearchTagCriteria;
import com.newpick4u.tag.domain.entity.Tag;
import com.newpick4u.tag.domain.repository.TagRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

  private final TagRepository tagRepository;

  @Override
  public Page<Tag> getTags(SearchTagCriteria criteria, Pageable pageable) {
    return tagRepository.searchByCriteria(criteria, pageable);
  }

  @Override
  @Transactional
  public UpdateTagRequestDto updateTag(UpdateTagRequestDto tag, UUID tagId) {
    Tag findTag = tagRepository.findById(tagId)
        .orElseThrow(() -> new CustomException(ApiErrorCode.NOT_FOUND));

    findTag.updateTagName(tag.tagName());

    Tag save = tagRepository.save(findTag);

    return new UpdateTagRequestDto(save.getTagName());
  }

  @Override
  @Transactional
  public void createTagFromAi(AiNewsDto dto) {
    dto.tagList().forEach(tagName -> {
      log.info("태그 조회 시도 중~~");
      log.info("태그명 : {}", tagName);
      Optional<Tag> tag = tagRepository.findByTagName(tagName); // 존재하는 태그는 score +1 처리
      if (tag.isPresent()) {
        Tag existingTag = tag.get();
        existingTag.increaseScore();
        return;
      }

      // 존재하지 않는 태그의 경우는 새로 생성해서 db에 저장
      log.info("태그 저장 시도 중~~");
      Tag newTag = Tag.create(tagName);
      tagRepository.save(newTag);

    });
  }

  @Override
  @Transactional
  public void deleteTagFromAi(AiNewsDto dto) {
    dto.tagList().forEach(tagName -> {
      Tag tag = tagRepository.findByTagName(tagName)
          .orElseThrow(() -> new IllegalArgumentException("해당 태그가 존재하지 않습니다."));

      if (tag.getScore() <= 1) {
        tagRepository.delete(tag);
      } else {
        tag.decreaseScore();
      }

    });
  }
}
