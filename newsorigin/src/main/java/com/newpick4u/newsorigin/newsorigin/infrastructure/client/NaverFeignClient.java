package com.newpick4u.newsorigin.newsorigin.infrastructure.client;

import com.newpick4u.newsorigin.global.config.FeignClientConfig;
import com.newpick4u.newsorigin.global.config.FeignNaverClientConfig;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "naver-service", url = "${app.client.naver.host}",
    configuration = {FeignClientConfig.class, FeignNaverClientConfig.class})
public interface NaverFeignClient {

  @GetMapping("${app.client.naver.collect-news.path}")
  String getSearchResult(@RequestParam Map<String, String> params);
  
}
