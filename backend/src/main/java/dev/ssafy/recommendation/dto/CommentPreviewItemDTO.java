package dev.ssafy.recommendation.dto;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * plan.md 3-3 기준
 * 목록용 댓글 preview DTO (최대 3개)
 * CommentItemDTO와 동일 구조
 */
@Getter
public class CommentPreviewItemDTO {

    private final Long id;
    private final String content;
    private final AuthorDTO author;
    private final FeedbackSummaryDTO feedbackSummary;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentPreviewItemDTO(Long id, String content, AuthorDTO author,
                                  FeedbackSummaryDTO feedbackSummary,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.feedbackSummary = feedbackSummary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
