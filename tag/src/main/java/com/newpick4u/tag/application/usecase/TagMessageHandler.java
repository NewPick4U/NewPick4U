package com.newpick4u.tag.application.usecase;

import com.newpick4u.tag.application.dto.AiNewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagMessageHandler {

  private final TagService tagService;

  public void handle(AiNewsDto dto) {
    tagService.createTagFromAi(dto);
  }

  public void deleteTagFromAi(AiNewsDto dto) {
    tagService.deleteTagFromAi(dto);
  }
}
