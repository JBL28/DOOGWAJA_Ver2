package dev.ssafy.bought_snack.dto;

import dev.ssafy.recommendation.dto.CommentPreviewItemDTO;
import dev.ssafy.recommendation.dto.FeedbackSummaryDTO;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BoughtSnackListItemDTO {

    private final Long bsId;
    private final String name;
    private final String status;
    private final String statusLabel;

    private final FeedbackSummaryDTO feedbackSummary;
    private final long commentCount;
    private final List<CommentPreviewItemDTO> commentPreview;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BoughtSnackListItemDTO(Long bsId, String name, String status, String statusLabel,
                                  FeedbackSummaryDTO feedbackSummary, long commentCount,
                                  List<CommentPreviewItemDTO> commentPreview,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.bsId = bsId;
        this.name = name;
        this.status = status;
        this.statusLabel = statusLabel;
        this.feedbackSummary = feedbackSummary;
        this.commentCount = commentCount;
        this.commentPreview = commentPreview;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
