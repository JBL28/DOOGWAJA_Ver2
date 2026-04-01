package dev.ssafy.common.dto;

import dev.ssafy.user.dto.UserNicknameDTO;
import java.util.List;

/**
 * 좋아요 표시한 사용자 목록 응답 DTO
 */
public record LikedUsersResponseDTO(
        List<UserNicknameDTO> users,
        boolean hasMore
) {}
