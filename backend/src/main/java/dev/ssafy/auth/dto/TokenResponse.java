package dev.ssafy.auth.dto;

/**
 * plan.md 3-4 A-4 기준 토큰 재발급 응답 data
 * api_spec.md 1-4: { accessToken }
 */
public record TokenResponse(String accessToken) {}
