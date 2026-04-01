package dev.ssafy.auth.controller;

import dev.ssafy.auth.dto.LoginRequest;
import dev.ssafy.auth.dto.LoginResponse;
import dev.ssafy.auth.dto.RegisterRequest;
import dev.ssafy.auth.dto.TokenResponse;
import dev.ssafy.auth.service.AuthService;
import dev.ssafy.common.ApiResponse;
import dev.ssafy.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * plan.md 3-1, 3-4 기준
 * POST /api/auth/register
 * POST /api/auth/login
 * POST /api/auth/logout
 * POST /api/auth/refresh
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * A-1. 회원가입 — 201 Created
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success(null, "회원가입이 완료되었습니다.");
    }

    /**
     * A-2. 로그인 — 200 OK
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request,
                                            HttpServletResponse response) {
        LoginResponse data = authService.login(request, response);
        return ApiResponse.success(data, "로그인되었습니다.");
    }

    /**
     * A-3. 로그아웃 — 200 OK (인증 필요)
     * SecurityContext의 principal은 CustomUserDetails
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    HttpServletResponse response) {
        authService.logout(userDetails.getUserId(), response);
        return ApiResponse.success(null, "로그아웃되었습니다.");
    }

    /**
     * A-4. Access Token 재발급 — 200 OK
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(HttpServletRequest request) {
        TokenResponse data = authService.refresh(request);
        return ApiResponse.success(data, "토큰이 재발급되었습니다.");
    }
}
