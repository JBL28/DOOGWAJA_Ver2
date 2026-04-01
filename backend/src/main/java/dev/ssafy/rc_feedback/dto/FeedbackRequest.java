package dev.ssafy.rc_feedback.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan.md RF-1, RCF-1 기준
 * 피드백 요청 DTO
 */
@Getter
@NoArgsConstructor
public class FeedbackRequest {

    @NotNull(message = "피드백 상태는 필수입니다.")
    private String status; // "LIKE" | "DISLIKE"
}
