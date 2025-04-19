package com.newpick4u.thread.thread.application.usecase;

import com.newpick4u.common.response.ApiResponse;
import com.newpick4u.thread.thread.infrastructure.client.dto.CommentResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;


public interface CommentClient {

  ResponseEntity<ApiResponse<CommentResponse>> getAllByThreadId(UUID uuid);
}
