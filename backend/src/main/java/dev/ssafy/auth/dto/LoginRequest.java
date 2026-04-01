package dev.ssafy.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * plan.md 3-4 A-2 기준 로그인 요청 DTO
 */
public record LoginRequest(
        @NotBlank(message = "아이디를 입력해주세요.")
        String loginId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {}
