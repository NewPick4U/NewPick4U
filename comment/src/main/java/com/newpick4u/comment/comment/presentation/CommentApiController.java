package com.newpick4u.comment.comment.presentation;

import com.newpick4u.comment.comment.application.CommentSearchCriteria;
import com.newpick4u.comment.comment.application.dto.CommentListPageDto.CommentContentDto;
import com.newpick4u.comment.comment.application.dto.CommentSaveRequestDto;
import com.newpick4u.comment.comment.application.dto.CommentUpdateDto;
import com.newpick4u.comment.comment.application.dto.GetCommentResponseDto;
import com.newpick4u.comment.comment.application.usecase.CommentFacadeService;
import com.newpick4u.comment.comment.application.usecase.CommentService;
import com.newpick4u.common.resolver.annotation.CurrentUserInfo;
import com.newpick4u.common.resolver.dto.CurrentUserInfoDto;
import com.newpick4u.common.response.ApiResponse;
import com.newpick4u.common.response.PageResponse;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@RestController
public class CommentApiController {

  private final CommentService commentService;
  private final CommentFacadeService commentFacadeService;

  // 댓글 등록 : 뉴스기사
  @PostMapping
  public ResponseEntity<ApiResponse<Map<String, UUID>>> saveCommentAtNews(
      @CurrentUserInfo CurrentUserInfoDto currentUserInfo,
      @RequestBody CommentSaveRequestDto requestDto
  ) {

    UUID savedCommentId;
    if (requestDto.isNewsComment()) {
      savedCommentId = commentFacadeService.saveCommentForNews(requestDto, currentUserInfo);
    } else {
      savedCommentId = commentService.saveCommentForThread(requestDto, currentUserInfo);
    }

    return ResponseEntity
        .status(HttpStatus.CREATED.value())
        .body(
            ApiResponse.of(
                HttpStatus.CREATED,
                "Success",
                Map.of("commentId", savedCommentId)
            )
        );
  }

  // 댓글 수정
  @PatchMapping("/{commentId}")
  public ResponseEntity<ApiResponse<Map<String, UUID>>> updateComment(
      @PathVariable("commentId") UUID commentId,
      @CurrentUserInfo CurrentUserInfoDto currentUserInfo,
      @RequestBody CommentUpdateDto requestDto
  ) {
    UUID updatedCommentId = commentService.updateComment(commentId, requestDto, currentUserInfo);

    return ResponseEntity
        .status(HttpStatus.OK.value())
        .body(
            ApiResponse.of(
                HttpStatus.OK,
                "Success",
                Map.of("commentId", updatedCommentId)
            )
        );
  }

  // 댓글 단일 조회
  @GetMapping("/{commentId}")
  public ResponseEntity<ApiResponse<GetCommentResponseDto>> getCommentById(
      @PathVariable("commentId") UUID commentId,
      @CurrentUserInfo CurrentUserInfoDto currentUserInfo
  ) {
    GetCommentResponseDto commentDto = commentService.getComment(commentId, currentUserInfo);

    return ResponseEntity
        .status(HttpStatus.OK.value())
        .body(
            ApiResponse.of(
                HttpStatus.OK,
                "Success",
                commentDto
            )
        );
  }

  // 댓글 목록 조회
  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<CommentContentDto>>> getCommentList(
      @ModelAttribute CommentSearchCriteria commentSearchCriteria,
      @CurrentUserInfo CurrentUserInfoDto currentUserInfo
  ) {
    PageResponse<CommentContentDto> commentList = commentService.getCommentList(
        commentSearchCriteria, currentUserInfo);

    return ResponseEntity
        .status(HttpStatus.OK.value())
        .body(
            ApiResponse.of(
                HttpStatus.OK,
                "Success",
                commentList
            )
        );
  }

  // 좋아요 등록
  @PostMapping("/{commentId}/goods")
  public ResponseEntity<ApiResponse<Map<String, Long>>> createCommentGood(
      @PathVariable("commentId") UUID commentId,
      @CurrentUserInfo CurrentUserInfoDto currentUserInfoDto
  ) {
    Long currentGoodCount = commentFacadeService.createGood(commentId, currentUserInfoDto);
    return ResponseEntity
        .status(HttpStatus.CREATED.value())
        .body(
            ApiResponse.of(
                HttpStatus.CREATED,
                "Success",
                Map.of("currentGoodCount", currentGoodCount)
            )
        );
  }

  // 좋아요 취소
  @DeleteMapping("/{commentId}/goods")
  public ResponseEntity<ApiResponse<Map<String, Long>>> deleteCommentGood(
      @PathVariable("commentId") UUID commentId,
      @CurrentUserInfo CurrentUserInfoDto currentUserInfoDto
  ) {
    Long currentGoodCount = commentFacadeService.deleteGood(commentId, currentUserInfoDto);
    return ResponseEntity
        .status(HttpStatus.OK.value())
        .body(
            ApiResponse.of(
                HttpStatus.OK,
                "Success",
                Map.of("currentGoodCount", currentGoodCount)
            )
        );
  }

}
