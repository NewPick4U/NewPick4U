package com.newpick4u.news.news.application.dto;

import java.util.List;
import java.util.UUID;

public record NewsTagDto(
        String aiNewsId,
        List<TagDto> tagList
) {
    public record TagDto(UUID id, String name) {}
}
