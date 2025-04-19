package com.newpick4u.thread.global.config;

import com.newpick4u.common.response.ApiResponse;
import com.newpick4u.thread.thread.application.usecase.CommentClient;
import com.newpick4u.thread.thread.infrastructure.client.dto.CommentResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CommentClientImplFallback implements CommentClient {

  @Override
  public ResponseEntity<ApiResponse<CommentResponse>> getAllByThreadId(UUID threadId) {
    return ResponseEntity
        .status(HttpStatus.CREATED.value())
        .body(
            ApiResponse.of(
                HttpStatus.OK,
                "Success",
                new CommentResponse(threadId, List.of())
            )
        );
  }
}
