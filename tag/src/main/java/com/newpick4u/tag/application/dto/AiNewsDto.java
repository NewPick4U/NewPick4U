package com.newpick4u.tag.application.dto;

import java.util.List;
import java.util.UUID;

public record AiNewsDto(
    UUID aiNewsId,
    List<String> tags
) {

}
