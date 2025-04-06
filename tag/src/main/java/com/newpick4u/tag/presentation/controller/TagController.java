package com.newpick4u.tag.presentation.controller;

import com.newpick4u.common.response.ApiResponse;
import com.newpick4u.common.response.PageResponse;
import com.newpick4u.tag.application.dto.TagListResponseDto;
import com.newpick4u.tag.application.dto.UpdateTagRequestDto;
import com.newpick4u.tag.application.usecase.TagService;
import com.newpick4u.tag.domain.criteria.SearchTagCriteria;
import com.newpick4u.tag.domain.entity.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

  private final TagService tagService;

  @GetMapping()
  public ResponseEntity<PageResponse<TagListResponseDto>> getTags(
      @RequestParam(required = false) String tagName,
      @RequestParam(required = false) Long minScore,
      @RequestParam(required = false) Long maxScore,
      Pageable pageable
  ) {

    SearchTagCriteria criteria = SearchTagCriteria.builder()
        .tagName(tagName)
        .minScore(minScore)
        .maxScore(maxScore)
        .build();

    Page<Tag> result = tagService.getTags(criteria, pageable);
    Page<TagListResponseDto> response = result.map(TagListResponseDto::from);

    return ResponseEntity.ok(PageResponse.from(response));
  }

  @PatchMapping("/{tagId}")
  public ResponseEntity<ApiResponse<UpdateTagRequestDto>> updateTag(@PathVariable UUID tagId,
      @RequestBody UpdateTagRequestDto tag) {
    return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, tagService.updateTag(tag, tagId)));
  }
}
