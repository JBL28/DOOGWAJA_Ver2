package dev.ssafy.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * plan.md 3-4 U-3 기준
 * api_spec.md 2-3: { password }
 */
public record DeleteUserRequest(
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {}
