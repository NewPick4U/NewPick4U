package com.newpick4u.comment.comment.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newpick4u.comment.comment.application.CommentSearchCriteria;
import com.newpick4u.comment.comment.application.NewsClient;
import com.newpick4u.comment.comment.application.TagCacheRepository;
import com.newpick4u.comment.comment.application.ThreadClient;
import com.newpick4u.comment.comment.application.UserClient;
import com.newpick4u.comment.comment.application.dto.CommentListPageDto.CommentContentDto;
import com.newpick4u.comment.comment.application.dto.CommentSaveRequestDto;
import com.newpick4u.comment.comment.application.dto.CommentUpdateDto;
import com.newpick4u.comment.comment.application.dto.GetCommentListForThreadResponseDto;
import com.newpick4u.comment.comment.application.dto.GetCommentResponseDto;
import com.newpick4u.comment.comment.application.dto.PointRequestDto;
import com.newpick4u.comment.comment.domain.entity.Comment;
import com.newpick4u.comment.comment.domain.entity.CommentGood;
import com.newpick4u.comment.comment.domain.repository.CommentGoodRepository;
import com.newpick4u.comment.comment.domain.repository.CommentRepository;
import com.newpick4u.comment.global.exception.CommentException;
import com.newpick4u.comment.global.exception.CommentException.CommentNotFoundException;
import com.newpick4u.comment.global.exception.CommentException.ConvertMessageFailException;
import com.newpick4u.comment.global.exception.CommentGoodException;
import com.newpick4u.common.resolver.dto.CurrentUserInfoDto;
import com.newpick4u.common.resolver.dto.UserRole;
import com.newpick4u.common.response.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final ObjectMapper objectMapper;
  private final ThreadClient threadClient;
  private final NewsClient newsClient;
  private final UserClient userClient;
  private final TagCacheRepository tagCacheRepository;
  private final CommentRepository commentRepository;
  private final CommentGoodRepository commentGoodRepository;

  // 댓글 저장 : 뉴스 댓글
  @Override
  public Map<String, Object> saveCommentForNews(CommentSaveRequestDto saveDto,
      CurrentUserInfoDto currentUserInfo) {

    // 뉴스 조회
    boolean existNews = newsClient.isExistNews(saveDto.newsId());
    if (!existNews) {
      throw new CommentException.NewsNotFoundException();
    }

    // 유저 이름 조회
    String username = userClient.getUsername(currentUserInfo.userId());

    Comment comment = Comment.createForNews(saveDto.newsId(), saveDto.content(), username);
    Comment savedComment = commentRepository.save(comment);

    String message = null;
    if (saveDto.isAdSet()) {
      message = getPointRequestMessage(saveDto, currentUserInfo, savedComment);
    }

    // 캐싱
    try {
      tagCacheRepository.increaseTagCount(saveDto.newsTags());
    } catch (Exception e) {
      log.error("Cache Save Fail : {}", saveDto.newsTags(), e);
      throw e;
    }

    return Map.of(
        "commentId", savedComment.getId(),
        "eventMessage", message);
  }

  private String getPointRequestMessage(CommentSaveRequestDto saveDto,
      CurrentUserInfoDto currentUserInfo,
      Comment comment) {

    PointRequestDto pointRequestDto = PointRequestDto.of(
        saveDto.advertisementId(),
        currentUserInfo.userId(),
        comment.getId()
    );

    try {
      String message = objectMapper.writeValueAsString(pointRequestDto);
      return message;
    } catch (JsonProcessingException e) {
      log.error("Make Json Message Fail : [userId={}][commentId={}][tags={}][content={}]",
          currentUserInfo.userId(), comment.getId(), saveDto.newsTags(), saveDto.content(), e);
      throw new ConvertMessageFailException();
    }
  }

  // 댓글 저장 : 쓰레드
  @Override
  public UUID saveCommentForThread(CommentSaveRequestDto saveDto,
      CurrentUserInfoDto currentUserInfo) {

    // 쓰레드 조회
    boolean existThread = threadClient.isExistThread(saveDto.threadId());
    if (!existThread) {
      throw new CommentException.ThreadNotFoundException();
    }

    // 유저 이름 조회
    String username = userClient.getUsername(currentUserInfo.userId());

    Comment comment = Comment.createForThread(saveDto.threadId(), saveDto.content(), username);
    commentRepository.save(comment);

    return comment.getId();
  }

  // 댓글 업데이트
  @Transactional
  @Override
  public UUID updateComment(UUID commentId, CommentUpdateDto updateDto,
      CurrentUserInfoDto userInfoDto) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentException.CommentNotFoundException());

    if (!Objects.equals(comment.getCreatedBy(), userInfoDto.userId())
        && userInfoDto.role() != UserRole.ROLE_MASTER) {
      throw new CommentException.PermissionDeniedException();
    }

    comment.updateContent(updateDto.content());
    return comment.getId();
  }

  @Transactional
  @Override
  public Long createGood(UUID commentId, CurrentUserInfoDto currentUserInfoDto) {

    // RDB 조회 및 중복체크
    Optional<CommentGood> findCommentGoodOptional = commentGoodRepository.findByCommentIdAndUserId(
        commentId, currentUserInfoDto.userId());
    if (findCommentGoodOptional.isPresent()) {
      throw new CommentGoodException.CommentGoodAlreadyExistException();
    }

    // 댓글 조회
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException());

    // 좋아요 추가 및 저장
    Long currentGoodCount = comment.addGood(currentUserInfoDto.userId());

    return currentGoodCount;
  }

  @Transactional
  @Override
  public Long deleteGood(UUID commentId, CurrentUserInfoDto currentUserInfoDto) {
    // RDB 조회 및 중복체크
    Optional<CommentGood> findCommentGoodOptional = commentGoodRepository.findByCommentIdAndUserId(
        commentId, currentUserInfoDto.userId());
    if (findCommentGoodOptional.isEmpty()) {
      throw new CommentGoodException.CommentGoodAlreadyDeletedException();
    }

    // 권한 체크
    CommentGood commentGood = findCommentGoodOptional.get();
    if (!Objects.equals(commentGood.getUserId(), currentUserInfoDto.userId())
        && currentUserInfoDto.role() != UserRole.ROLE_MASTER
    ) {
      throw new CommentGoodException.PermissionDeniedException();
    }

    Comment comment = commentGood.getComment();

    Long currentGoodCount = comment.deleteGood(commentGood);
    return currentGoodCount;
  }

  // 단일 조회
  @Override
  public GetCommentResponseDto getComment(UUID commentId, CurrentUserInfoDto currentUserInfoDto) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException());
    Optional<CommentGood> existCommentGood = commentGoodRepository.findByCommentIdAndUserId(
        comment.getId(), currentUserInfoDto.userId());
    return GetCommentResponseDto.of(comment, existCommentGood.isPresent());
  }

  // 목록 조회
  @Override
  public PageResponse<CommentContentDto> getCommentList(
      CommentSearchCriteria commentSearchCriteria,
      CurrentUserInfoDto currentUserInfoDto) {
    return commentRepository.findCommentsWithUserGood(
        currentUserInfoDto.userId(), commentSearchCriteria);
  }

  // 쓰레드 댓글 전체 조회
  @Transactional(readOnly = true)
  @Override
  public GetCommentListForThreadResponseDto getCommentByThreadId(UUID threadId) {
    List<Comment> threadCommentList = commentRepository.findAllByThreadIdAndDeletedAtIsNull(
        threadId);
    List<String> contentList = threadCommentList.stream().map(Comment::getContent).toList();
    return GetCommentListForThreadResponseDto.of(contentList, threadId);
  }
}

