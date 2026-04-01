package dev.ssafy.recommendation.dto;

import lombok.Getter;

/**
 * plan.md 3-3 기준
 * feedbackSummary: likeCount, dislikeCount, myFeedback (null 가능)
 */
@Getter
public class FeedbackSummaryDTO {

    private final long likeCount;
    private final long dislikeCount;
    private final String myFeedback; // "LIKE" | "DISLIKE" | null

    public FeedbackSummaryDTO(long likeCount, long dislikeCount, String myFeedback) {
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.myFeedback = myFeedback;
    }
}
