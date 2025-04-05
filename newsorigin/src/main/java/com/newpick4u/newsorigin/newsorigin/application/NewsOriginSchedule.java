package com.newpick4u.newsorigin.newsorigin.application;

import com.newpick4u.newsorigin.newsorigin.application.usecase.NewsOriginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NewsOriginSchedule {

  private final NewsOriginService newsOriginService;

  // @Scheduled(cron = "0 */5 * * * *")
  public void originNewsCollectSchedule() {
    log.info("Start OriginNews Collect =========================");
    int collectCount = newsOriginService.collectOriginNews();
    log.info("End OriginNews Collect : collectCount = {} =====", collectCount);
  }
}
