package dev.ssafy.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * plan.md 3-4 A-1 기준 회원가입 요청 DTO
 * api_spec.md 1-1 유효성 조건
 */
public record RegisterRequest(

        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(min = 5, message = "loginId는 5자 이상이어야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "loginId는 5자 이상이어야 합니다.")
        String loginId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).+$",
                message = "비밀번호는 영문과 숫자를 포함해야 합니다.")
        String password,

        @NotBlank(message = "nickname은 1자 이상이어야 합니다.")
        String nickname
) {}
