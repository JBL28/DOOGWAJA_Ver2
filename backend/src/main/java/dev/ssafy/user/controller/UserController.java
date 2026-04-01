package dev.ssafy.user.controller;

import dev.ssafy.common.ApiResponse;
import dev.ssafy.user.dto.DeleteUserRequest;
import dev.ssafy.user.dto.UpdateUserRequest;
import dev.ssafy.user.dto.UpdateUserResponse;
import dev.ssafy.user.dto.UserMeResponse;
import dev.ssafy.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * plan.md 3-1, 3-4 기준
 * GET /api/users/me
 * PUT /api/users/me
 * DELETE /api/users/me
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * U-1. 내 정보 조회
     */
    @GetMapping("/me")
    public ApiResponse<UserMeResponse> getMe(@AuthenticationPrincipal Long userId) {
        UserMeResponse data = userService.getMe(userId);
        return ApiResponse.success(data, "요청이 성공적으로 처리되었습니다.");
    }

    /**
     * U-2. 내 정보 수정
     */
    @PutMapping("/me")
    public ApiResponse<UpdateUserResponse> updateMe(@AuthenticationPrincipal Long userId,
                                                    @RequestBody @Valid UpdateUserRequest request) {
        UpdateUserResponse data = userService.updateMe(userId, request);
        return ApiResponse.success(data, "정보가 수정되었습니다.");
    }

    /**
     * U-3. 회원 탈퇴
     */
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMe(@AuthenticationPrincipal Long userId,
                                      @RequestBody @Valid DeleteUserRequest request,
                                      HttpServletResponse response) {
        userService.deleteMe(userId, request, response);
        return ApiResponse.success(null, "회원 탈퇴가 완료되었습니다.");
    }
}
