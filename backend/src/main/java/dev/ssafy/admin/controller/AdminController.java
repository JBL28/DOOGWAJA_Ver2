package dev.ssafy.admin.controller;

import dev.ssafy.admin.dto.UserListResponseDto;
import dev.ssafy.admin.dto.UserStatusUpdateRequestDto;
import dev.ssafy.admin.service.AdminService;
import dev.ssafy.common.ApiResponse;
import dev.ssafy.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserListResponseDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<UserListResponseDto> result = PageResponse.from(
                adminService.getAllUsers(PageRequest.of(page, size)));
        return ResponseEntity.ok(ApiResponse.success(result, "전체 유저 목록을 조회했습니다."));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateRequestDto request
    ) {
        adminService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(null, "유저 상태가 변경되었습니다."));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId
    ) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "유저가 삭제 처리되었습니다."));
    }
}
