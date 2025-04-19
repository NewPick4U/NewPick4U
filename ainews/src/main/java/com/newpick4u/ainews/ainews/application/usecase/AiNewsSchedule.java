package com.newpick4u.ainews.ainews.application.usecase;

import com.newpick4u.ainews.ainews.application.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AiNewsSchedule {

  private final AiClient aiClient;

  @Scheduled(cron = "2 * * * * *")
  public void init() {
    aiClient.initRemainAvailableRequestCountPerMin();
  }
}
