package dev.ssafy.user.dto;

/**
 * 좋아요 목록 등에 사용되는 최소한의 사용자 정보 DTO
 */
public record UserNicknameDTO(
        Long userId,
        String nickname
) {}
