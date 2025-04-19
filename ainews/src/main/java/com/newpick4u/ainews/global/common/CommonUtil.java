package com.newpick4u.ainews.global.common;

public class CommonUtil {

  public static String formatMillisToTime(long millis) {
    long totalSeconds = millis / 1000;

    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;

    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }
}
