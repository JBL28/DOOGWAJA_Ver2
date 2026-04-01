package dev.ssafy.bought_snack.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.ssafy.bought_snack.dto.BoughtSnackCreateRequest;
import dev.ssafy.bought_snack.dto.BoughtSnackDetailDTO;
import dev.ssafy.bought_snack.dto.BoughtSnackListItemDTO;
import dev.ssafy.bought_snack.dto.BoughtSnackStatusUpdateRequest;
import dev.ssafy.bought_snack.dto.BoughtSnackUpdateRequest;
import dev.ssafy.bought_snack.service.BoughtSnackService;
import dev.ssafy.bs_comment.dto.BsCommentCreateRequest;
import dev.ssafy.bs_comment.dto.BsCommentUpdateRequest;
import dev.ssafy.bs_comment.service.BsCommentService;
import dev.ssafy.bs_feedback.service.BsFeedbackService;
import dev.ssafy.bsc_feedback.service.BscFeedbackService;
import dev.ssafy.common.dto.LikedUsersResponseDTO;
import dev.ssafy.common.ApiResponse;
import dev.ssafy.common.PageResponse;
import dev.ssafy.rc_comment.dto.CommentItemDTO;
import dev.ssafy.rc_feedback.dto.FeedbackRequest;
import dev.ssafy.rc_feedback.dto.FeedbackResultDTO;
import dev.ssafy.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * plan.md Phase 1, 2, 3, 4 기준
 * 구매 과자 + 댓글 + 피드백 통합
 */
@RestController
@RequestMapping("/api/bought-snacks")
@RequiredArgsConstructor
public class BoughtSnackController {

    private final BoughtSnackService boughtSnackService;
    private final BsCommentService bsCommentService;
    private final BsFeedbackService bsFeedbackService;
    private final BscFeedbackService bscFeedbackService;

    // ===== 구매 과자 =====

    /**
     * 7-1: GET /api/bought-snacks?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BoughtSnackListItemDTO>>> getBoughtSnacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        PageResponse<BoughtSnackListItemDTO> result = PageResponse.from(
                boughtSnackService.getBoughtSnacks(page, size, userId));
        return ResponseEntity.ok(ApiResponse.success(result, "구매 과자 목록을 조회했습니다."));
    }

    /**
     * 7-2: GET /api/bought-snacks/{bsId}
     */
    @GetMapping("/{bsId}")
    public ResponseEntity<ApiResponse<BoughtSnackDetailDTO>> getBoughtSnack(
            @PathVariable Long bsId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        BoughtSnackDetailDTO result = boughtSnackService.getBoughtSnack(bsId, userId);
        return ResponseEntity.ok(ApiResponse.success(result, "구매 과자를 조회했습니다."));
    }

    /**
     * 7-3: POST /api/bought-snacks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createBoughtSnack(
            @Valid @RequestBody BoughtSnackCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long bsId = boughtSnackService.createBoughtSnack(request, userDetails.getUser().getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("bsId", bsId), "과자가 등록되었습니다."));
    }

    /**
     * 7-4: PUT /api/bought-snacks/{bsId}
     */
    @PutMapping("/{bsId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> updateBoughtSnack(
            @PathVariable Long bsId,
            @Valid @RequestBody BoughtSnackUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long updatedId = boughtSnackService.updateBoughtSnack(bsId, request, userDetails.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(Map.of("bsId", updatedId), "과자 정보가 수정되었습니다."));
    }

    /**
     * 7-5: DELETE /api/bought-snacks/{bsId}
     */
    @DeleteMapping("/{bsId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoughtSnack(
            @PathVariable Long bsId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boughtSnackService.deleteBoughtSnack(bsId, userDetails.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(null, "과자가 삭제되었습니다."));
    }

    /**
     * 7-6: PATCH /api/bought-snacks/{bsId}/status
     */
    @PatchMapping("/{bsId}/status")
    public ResponseEntity<ApiResponse<BoughtSnackDetailDTO>> updateBoughtSnackStatus(
            @PathVariable Long bsId,
            @Valid @RequestBody BoughtSnackStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        BoughtSnackDetailDTO result = boughtSnackService.updateBoughtSnackStatus(bsId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(result, "상태가 변경되었습니다."));
    }

    // ===== 구매 과자 댓글 =====

    /**
     * 8-1: GET /api/bought-snacks/{bsId}/comments?page=0&size=10
     */
    @GetMapping("/{bsId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentItemDTO>>> getComments(
            @PathVariable Long bsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        PageResponse<CommentItemDTO> result = PageResponse.from(
                bsCommentService.getComments(bsId, page, size, userId));
        return ResponseEntity.ok(ApiResponse.success(result, "댓글 목록을 조회했습니다."));
    }

    /**
     * 8-2: POST /api/bought-snacks/{bsId}/comments
     */
    @PostMapping("/{bsId}/comments")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createComment(
            @PathVariable Long bsId,
            @Valid @RequestBody BsCommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long bscId = bsCommentService.createComment(
                bsId, request, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("bscId", bscId), "댓글이 작성되었습니다."));
    }

    /**
     * 8-3: PUT /api/bought-snacks/{bsId}/comments/{bscId}
     */
    @PutMapping("/{bsId}/comments/{bscId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> updateComment(
            @PathVariable Long bsId,
            @PathVariable Long bscId,
            @Valid @RequestBody BsCommentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long updatedId = bsCommentService.updateComment(bscId, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("bscId", updatedId), "댓글이 수정되었습니다."));
    }

    /**
     * 8-4: DELETE /api/bought-snacks/{bsId}/comments/{bscId}
     */
    @DeleteMapping("/{bsId}/comments/{bscId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long bsId,
            @PathVariable Long bscId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        bsCommentService.deleteComment(bscId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 삭제되었습니다."));
    }

    // ===== 피드백 =====

    /**
     * 9-1: POST /api/bought-snacks/{bsId}/feedback
     */
    @PostMapping("/{bsId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResultDTO>> toggleBsFeedback(
            @PathVariable Long bsId,
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FeedbackResultDTO result = bsFeedbackService.toggleFeedback(
                bsId, request, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(result, "피드백이 반영되었습니다."));
    }

    /**
     * 구매 과자 좋아요 표시한 유저 목록 조회
     */
    @GetMapping("/{bsId}/likes")
    public ResponseEntity<ApiResponse<LikedUsersResponseDTO>> getLikedUsers(@PathVariable Long bsId) {
        LikedUsersResponseDTO result = bsFeedbackService.getLikedUsers(bsId);
        return ResponseEntity.ok(ApiResponse.success(result, "좋아요 표시한 유저 목록을 조회했습니다."));
    }

    /**
     * 10-1: POST /api/bought-snacks/{bsId}/comments/{bscId}/feedback
     */
    @PostMapping("/{bsId}/comments/{bscId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResultDTO>> toggleBscFeedback(
            @PathVariable Long bsId,
            @PathVariable Long bscId,
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FeedbackResultDTO result = bscFeedbackService.toggleFeedback(
                bscId, request, userDetails.getUserId(), userDetails.getUser().getRole());
        return ResponseEntity.ok(ApiResponse.success(result, "피드백이 반영되었습니다."));
    }
}
