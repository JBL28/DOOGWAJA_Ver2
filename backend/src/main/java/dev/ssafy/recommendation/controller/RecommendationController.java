package dev.ssafy.recommendation.controller;

import dev.ssafy.common.ApiResponse;
import dev.ssafy.common.PageResponse;
import dev.ssafy.rc_comment.dto.CommentItemDTO;
import dev.ssafy.rc_comment.dto.RcCommentCreateRequest;
import dev.ssafy.rc_comment.dto.RcCommentUpdateRequest;
import dev.ssafy.rc_comment.service.RcCommentService;
import dev.ssafy.rc_feedback.dto.FeedbackRequest;
import dev.ssafy.rc_feedback.dto.FeedbackResultDTO;
import dev.ssafy.rc_feedback.service.RcFeedbackService;
import dev.ssafy.rcc_feedback.service.RccFeedbackService;
import dev.ssafy.recommendation.dto.RecommendationCreateRequest;
import dev.ssafy.recommendation.dto.RecommendationDetailDTO;
import dev.ssafy.recommendation.dto.RecommendationListItemDTO;
import dev.ssafy.recommendation.dto.RecommendationUpdateRequest;
import dev.ssafy.recommendation.service.RecommendationService;
import dev.ssafy.security.CustomUserDetails;
import dev.ssafy.common.dto.LikedUsersResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * plan.md Phase 3, 1-1 기준
 * 추천글 + 추천 댓글 + 피드백 모든 엔드포인트 통합
 *
 * R-1 ~ R-5, RC-1 ~ RC-4, RF-1, RCF-1
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RcCommentService rcCommentService;
    private final RcFeedbackService rcFeedbackService;
    private final RccFeedbackService rccFeedbackService;

    // ===== 추천글 =====

    /**
     * R-1: GET /api/recommendations?page=0&size=10
     * 추천글 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RecommendationListItemDTO>>> getRecommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        PageResponse<RecommendationListItemDTO> result = PageResponse.from(
                recommendationService.getRecommendations(page, size, userId));
        return ResponseEntity.ok(ApiResponse.success(result, "추천글 목록을 조회했습니다."));
    }

    /**
     * R-2: GET /api/recommendations/{rcId}
     * 추천글 단건 조회
     */
    @GetMapping("/{rcId}")
    public ResponseEntity<ApiResponse<RecommendationDetailDTO>> getRecommendation(
            @PathVariable Long rcId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        RecommendationDetailDTO result = recommendationService.getRecommendation(rcId, userId);
        return ResponseEntity.ok(ApiResponse.success(result, "추천글을 조회했습니다."));
    }

    /**
     * R-3: POST /api/recommendations
     * 추천글 생성 (USER only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createRecommendation(
            @Valid @RequestBody RecommendationCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long rcId = recommendationService.createRecommendation(
                request, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("rcId", rcId), "추천글이 등록되었습니다."));
    }

    /**
     * R-4: PUT /api/recommendations/{rcId}
     * 추천글 수정 (본인만)
     */
    @PutMapping("/{rcId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> updateRecommendation(
            @PathVariable Long rcId,
            @Valid @RequestBody RecommendationUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long updatedId = recommendationService.updateRecommendation(rcId, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("rcId", updatedId), "추천글이 수정되었습니다."));
    }

    /**
     * R-5: DELETE /api/recommendations/{rcId}
     * 추천글 삭제 (본인만)
     */
    @DeleteMapping("/{rcId}")
    public ResponseEntity<ApiResponse<Void>> deleteRecommendation(
            @PathVariable Long rcId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recommendationService.deleteRecommendation(rcId, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(null, "추천글이 삭제되었습니다."));
    }

    // ===== 추천 댓글 =====

    /**
     * RC-1: GET /api/recommendations/{rcId}/comments?page=0&size=10
     * 추천 댓글 전체 조회
     */
    @GetMapping("/{rcId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentItemDTO>>> getComments(
            @PathVariable Long rcId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        PageResponse<CommentItemDTO> result = PageResponse.from(
                rcCommentService.getComments(rcId, page, size, userId));
        return ResponseEntity.ok(ApiResponse.success(result, "댓글 목록을 조회했습니다."));
    }

    /**
     * RC-2: POST /api/recommendations/{rcId}/comments
     * 추천 댓글 작성 (USER only)
     */
    @PostMapping("/{rcId}/comments")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createComment(
            @PathVariable Long rcId,
            @Valid @RequestBody RcCommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long rccId = rcCommentService.createComment(
                rcId, request, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("rccId", rccId), "댓글이 작성되었습니다."));
    }

    /**
     * RC-3: PUT /api/recommendations/{rcId}/comments/{rccId}
     * 추천 댓글 수정 (본인만)
     */
    @PutMapping("/{rcId}/comments/{rccId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> updateComment(
            @PathVariable Long rcId,
            @PathVariable Long rccId,
            @Valid @RequestBody RcCommentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long updatedId = rcCommentService.updateComment(rccId, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("rccId", updatedId), "댓글이 수정되었습니다."));
    }

    /**
     * RC-4: DELETE /api/recommendations/{rcId}/comments/{rccId}
     * 추천 댓글 삭제 (본인만)
     */
    @DeleteMapping("/{rcId}/comments/{rccId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long rcId,
            @PathVariable Long rccId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        rcCommentService.deleteComment(rccId, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 삭제되었습니다."));
    }

    // ===== 피드백 =====

    /**
     * RF-1: POST /api/recommendations/{rcId}/feedback
     * 추천글 피드백 toggle (USER only)
     */
    @PostMapping("/{rcId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResultDTO>> toggleRcFeedback(
            @PathVariable Long rcId,
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FeedbackResultDTO result = rcFeedbackService.toggleFeedback(
                rcId, request, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(result, "피드백이 처리되었습니다."));
    }

    /**
     * 추천글 좋아요 표시한 유저 목록 조회
     */
    @GetMapping("/{rcId}/likes")
    public ResponseEntity<ApiResponse<LikedUsersResponseDTO>> getLikedUsers(@PathVariable Long rcId) {
        LikedUsersResponseDTO result = rcFeedbackService.getLikedUsers(rcId);
        return ResponseEntity.ok(ApiResponse.success(result, "좋아요 표시한 유저 목록을 조회했습니다."));
    }

    /**
     * RCF-1: POST /api/recommendations/{rcId}/comments/{rccId}/feedback
     * 추천 댓글 피드백 toggle (USER only)
     */
    @PostMapping("/{rcId}/comments/{rccId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResultDTO>> toggleRccFeedback(
            @PathVariable Long rcId,
            @PathVariable Long rccId,
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FeedbackResultDTO result = rccFeedbackService.toggleFeedback(
                rccId, request, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(result, "댓글 피드백이 처리되었습니다."));
    }
}
