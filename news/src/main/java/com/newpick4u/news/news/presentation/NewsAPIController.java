package com.newpick4u.news.news.presentation;

import com.newpick4u.common.resolver.annotation.CurrentUserInfo;
import com.newpick4u.common.resolver.dto.CurrentUserInfoDto;
import com.newpick4u.news.news.application.dto.response.NewsResponseDto;
import com.newpick4u.news.news.application.dto.response.NewsSummaryDto;
import com.newpick4u.news.news.application.dto.response.PageResponse;
import com.newpick4u.news.news.application.usecase.NewsService;
import com.newpick4u.news.news.application.dto.NewsSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameters;
import com.newpick4u.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "뉴스 API", description = "뉴스 API")
public class NewsAPIController {
    private final NewsService newsService;

    @GetMapping("/{id}")
    @Operation(summary = "뉴스 조회", description = "특정 뉴스를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "뉴스 조회 성공")
    public ResponseEntity<ApiResponse<NewsResponseDto>> getNews(
            @PathVariable UUID id,
            @CurrentUserInfo @Parameter(hidden = true) CurrentUserInfoDto userInfoDto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getNews(id, userInfoDto)));
    }

    @GetMapping
    @Operation(summary = "뉴스 리스트 조회", description = "뉴스 리스트를 검색 및 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "뉴스 리스트 조회 성공")
    @Parameters({
            @Parameter(name = "keyword", description = "검색 키워드"),
            @Parameter(name = "filter", description = "필터 조건 (e.g., title, tag)"),
            @Parameter(name = "sort", description = "정렬 기준 (e.g., latest, view)"),
            @Parameter(name = "page", description = "페이지 번호"),
            @Parameter(name = "size", description = "페이지 사이즈")
    })
    public ResponseEntity<ApiResponse<PageResponse<NewsSummaryDto>>> searchNewsList(
            @ModelAttribute NewsSearchCriteria request,
            @CurrentUserInfo @Parameter(hidden = true) CurrentUserInfoDto userInfoDto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.searchNewsList(request, userInfoDto)));
    }

    @GetMapping("/recommend")
    @Operation(summary = "추천 뉴스 리스트 조회", description = "10개의 추천 뉴스를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "뉴스 추천 리스트 조회 성공")
    public ResponseEntity<ApiResponse<List<NewsSummaryDto>>> recommendNewsList(
            @CurrentUserInfo @Parameter(hidden = true) CurrentUserInfoDto currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.recommendTop10(currentUser)));
    }

    @GetMapping("/popular")
    @Operation(summary = "추천 뉴스 리스트 조회", description = "10개의 인기 뉴스를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "뉴스 인기 리스트 조회 성공")
    public ResponseEntity<ApiResponse<List<NewsSummaryDto>>> getPopularNewsList() {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getPopularTop10()));
    }


    @DeleteMapping("/{newsId}")
    @Operation(summary = "뉴스 삭제", description = "권한에 따라 뉴스를 소프트 삭제합니다.")public ResponseEntity<ApiResponse<Map<String, UUID>>> deleteNews(
            @PathVariable UUID newsId,
            @CurrentUserInfo @Parameter(hidden = true) CurrentUserInfoDto userInfo
    ) {
        newsService.deleteNews(newsId, userInfo);
        return ResponseEntity.ok(
                ApiResponse.of(HttpStatus.OK, Map.of("뉴스 삭제 완료", newsId))
        );
    }

}
