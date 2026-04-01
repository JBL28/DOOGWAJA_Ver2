package dev.ssafy.recommendation.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * plan.md 3-3 기준
 * 추천글 목록 조회 응답 DTO (R-1)
 */
@Getter
public class RecommendationListItemDTO {

    private final Long rcId;
    private final String name;
    private final String reason;
    private final AuthorDTO author;
    private final FeedbackSummaryDTO feedbackSummary;
    private final long commentCount;
    private final List<CommentPreviewItemDTO> commentPreview;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RecommendationListItemDTO(Long rcId, String name, String reason,
                                      AuthorDTO author, FeedbackSummaryDTO feedbackSummary,
                                      long commentCount, List<CommentPreviewItemDTO> commentPreview,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.rcId = rcId;
        this.name = name;
        this.reason = reason;
        this.author = author;
        this.feedbackSummary = feedbackSummary;
        this.commentCount = commentCount;
        this.commentPreview = commentPreview;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
