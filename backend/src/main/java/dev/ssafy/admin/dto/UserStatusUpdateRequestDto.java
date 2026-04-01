package dev.ssafy.admin.dto;

import dev.ssafy.user.entity.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatusUpdateRequestDto {

    private User.Status status;

    public UserStatusUpdateRequestDto(User.Status status) {
        this.status = status;
    }
}
