package dev.ssafy.rc_comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan.md RC-3 기준
 * 추천 댓글 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class RcCommentUpdateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
