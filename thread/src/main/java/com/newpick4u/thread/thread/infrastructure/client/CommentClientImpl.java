package com.newpick4u.thread.thread.infrastructure.client;

import com.newpick4u.common.response.ApiResponse;
import com.newpick4u.thread.thread.application.usecase.CommentClient;
import com.newpick4u.thread.thread.infrastructure.client.dto.CommentResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentClientImpl implements CommentClient {

  private final CommentFeignClient commentFeignClient;

  @Override
  public ResponseEntity<ApiResponse<CommentResponse>> getAllByThreadId(UUID uuid) {
    return commentFeignClient.getAllByThreadId(uuid);
  }

}
