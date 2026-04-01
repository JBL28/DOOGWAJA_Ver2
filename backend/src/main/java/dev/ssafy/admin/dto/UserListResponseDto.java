package dev.ssafy.admin.dto;

import dev.ssafy.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserListResponseDto {

    private Long userId;
    private String loginId;
    private String nickname;
    private User.Role role;
    private User.Status status;
    private LocalDateTime createdAt;

    @Builder
    public UserListResponseDto(Long userId, String loginId, String nickname, User.Role role, User.Status status, LocalDateTime createdAt) {
        this.userId = userId;
        this.loginId = loginId;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static UserListResponseDto from(User user) {
        return UserListResponseDto.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
