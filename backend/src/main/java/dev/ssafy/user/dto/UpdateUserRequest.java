package dev.ssafy.user.dto;

import jakarta.validation.constraints.Pattern;

/**
 * plan.md 3-4 U-2 기준
 * api_spec.md 2-2: nickname (선택), currentPassword (newPassword 있을 시 필수), newPassword (선택)
 */
public record UpdateUserRequest(
        String nickname,
        String currentPassword,

        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).+$",
                message = "새 비밀번호는 영문과 숫자를 포함해야 합니다.")
        String newPassword
) {}
