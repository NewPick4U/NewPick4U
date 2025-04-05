package com.newpick4u.newsorigin.newsorigin.application.dto;

import java.time.LocalDateTime;

public record NewNewsOriginDto(
    LocalDateTime publishedDate,
    String url
) {

}
