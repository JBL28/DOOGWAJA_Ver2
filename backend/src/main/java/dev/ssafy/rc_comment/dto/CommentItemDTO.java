package dev.ssafy.rc_comment.dto;

import dev.ssafy.recommendation.dto.AuthorDTO;
import dev.ssafy.recommendation.dto.FeedbackSummaryDTO;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * plan.md 3-3 기준
 * 댓글 전체 조회용 DTO (RC-1)
 * CommentPreviewItemDTO와 동일 구조
 */
@Getter
public class CommentItemDTO {

    private final Long id;
    private final String content;
    private final AuthorDTO author;
    private final FeedbackSummaryDTO feedbackSummary;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentItemDTO(Long id, String content, AuthorDTO author,
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
