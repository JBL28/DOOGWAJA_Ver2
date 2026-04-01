package dev.ssafy.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan.md R-3 기준
 * 추천글 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
public class RecommendationCreateRequest {

    @NotBlank(message = "과자 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "추천 이유는 필수입니다.")
    private String reason;
}
