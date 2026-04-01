package dev.ssafy.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan.md R-4 기준
 * 추천글 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class RecommendationUpdateRequest {

    @NotBlank(message = "과자 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "추천 이유는 필수입니다.")
    private String reason;
}
