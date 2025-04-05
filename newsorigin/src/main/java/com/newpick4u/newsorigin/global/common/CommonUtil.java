package com.newpick4u.newsorigin.global.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CommonUtil {

  /**
   * 특정 포멧의 시간 정보를 LocalDateTime 형태로 변경
   *
   * @param timeString "Sat, 05 Apr 2025 23:00:06 +0900" 형태의 String
   * @return LocalDateTime
   */
  public static LocalDateTime convertStringToLocalDateTime(final String timeString) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH
    );

    OffsetDateTime offsetDateTime = OffsetDateTime.parse(timeString, formatter);
    return offsetDateTime.toLocalDateTime();
  }
}
