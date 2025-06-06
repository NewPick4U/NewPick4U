package com.newpick4u.user.application.message.request;

import java.util.UUID;

public record PointRequestFailureMessage(Long userId, UUID advertisementId) {

  public static PointRequestFailureMessage of(Long userId, UUID advertisementId) {
    return new PointRequestFailureMessage(userId, advertisementId);
  }
}
