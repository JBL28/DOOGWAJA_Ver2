package dev.ssafy.rc_feedback.dto;

import lombok.Getter;

/**
 * plan.md RF-1, RCF-1 기준
 * 피드백 응답 DTO
 * myFeedback: "LIKE" | "DISLIKE" | null (취소 시 null)
 */
@Getter
public class FeedbackResultDTO {

    private final String myFeedback;
    private final long likeCount;
    private final long dislikeCount;

    public FeedbackResultDTO(String myFeedback, long likeCount, long dislikeCount) {
        this.myFeedback = myFeedback;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }
}
