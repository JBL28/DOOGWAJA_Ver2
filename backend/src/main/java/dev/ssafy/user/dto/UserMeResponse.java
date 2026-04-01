package dev.ssafy.user.dto;

import dev.ssafy.user.entity.User;

import java.time.LocalDateTime;

/**
 * plan.md 3-4 U-1 기준
 * api_spec.md 2-1: userId, loginId, nickname, role, status, createdAt
 */
public record UserMeResponse(
        Long userId,
        String loginId,
        String nickname,
        String role,
        String status,
        LocalDateTime createdAt
) {
    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}
