package dev.ssafy.recommendation.dto;

import lombok.Getter;

/**
 * plan.md 3-3 기준
 * 작성자 정보 DTO
 */
@Getter
public class AuthorDTO {

    private final Long userId;
    private final String nickname;

    public AuthorDTO(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
