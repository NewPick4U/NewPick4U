package com.newpick4u.thread.thread.infrastructure.client;

import com.newpick4u.common.response.ApiResponse;
import com.newpick4u.thread.global.config.CommentClientFallback;
import com.newpick4u.thread.global.config.FeignClientConfig;
import com.newpick4u.thread.thread.infrastructure.client.dto.CommentResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "comment-service", configuration = FeignClientConfig.class, fallback = CommentClientFallback.class)
public interface CommentClient {

  @GetMapping("/internal/v1/comments/threads/{threadId}")
  ResponseEntity<ApiResponse<CommentResponse>> getAllByThreadId(@PathVariable UUID threadId);

}
