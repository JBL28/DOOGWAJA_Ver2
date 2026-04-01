package dev.ssafy.user.dto;

/**
 * plan.md 3-4 U-2 기준
 * api_spec.md 2-2: { userId, nickname }
 */
public record UpdateUserResponse(Long userId, String nickname) {}
