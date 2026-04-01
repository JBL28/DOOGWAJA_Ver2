package dev.ssafy.auth.dto;

/**
 * plan.md 3-4 A-2 기준 로그인 성공 응답 data
 * api_spec.md 1-2: accessToken, userId, loginId, nickname, role
 */
public record LoginResponse(
        String accessToken,
        Long userId,
        String loginId,
        String nickname,
        String role
) {}
