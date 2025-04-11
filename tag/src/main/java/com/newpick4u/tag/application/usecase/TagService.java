package com.newpick4u.tag.application.usecase;

import com.newpick4u.tag.application.dto.AiNewsDto;
import com.newpick4u.tag.application.dto.UpdateTagRequestDto;
import com.newpick4u.tag.domain.criteria.SearchTagCriteria;
import com.newpick4u.tag.domain.entity.Tag;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagService {

  Page<Tag> getTags(SearchTagCriteria criteria, Pageable pageable);

  UpdateTagRequestDto updateTag(UpdateTagRequestDto tag, UUID tagId);

  void createTagFromAi(AiNewsDto dto);

  void deleteTagFromAi(AiNewsDto dto);
}
