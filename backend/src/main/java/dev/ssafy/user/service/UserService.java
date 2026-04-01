package dev.ssafy.user.service;

import dev.ssafy.auth.util.CookieUtil;
import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.common.exception.PasswordMismatchException;
import dev.ssafy.user.dto.DeleteUserRequest;
import dev.ssafy.user.dto.UpdateUserRequest;
import dev.ssafy.user.dto.UpdateUserResponse;
import dev.ssafy.user.dto.UserMeResponse;
import dev.ssafy.user.entity.User;
import dev.ssafy.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * plan.md Phase 4 Step 13 기준
 * U-1 getMe / U-2 updateMe / U-3 deleteMe
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;

    /**
     * U-1. 내 정보 조회 — plan.md 3-4 U-1
     */
    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        return UserMeResponse.from(user);
    }

    /**
     * U-2. 내 정보 수정 — plan.md 3-4 U-2
     * - nickname만 있으면 닉네임 변경
     * - newPassword 있으면 currentPassword 필수 → 검증 → 유효성 검증 → 해시 저장
     */
    @Transactional
    public UpdateUserResponse updateMe(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 닉네임 수정
        if (StringUtils.hasText(request.nickname())) {
            user.updateNickname(request.nickname());
        }

        // 비밀번호 변경
        if (StringUtils.hasText(request.newPassword())) {
            // currentPassword 필수
            if (!StringUtils.hasText(request.currentPassword())) {
                throw new PasswordMismatchException("현재 비밀번호가 일치하지 않습니다.");
            }
            // 현재 비밀번호 검증
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordH())) {
                throw new PasswordMismatchException("현재 비밀번호가 일치하지 않습니다.");
            }
            // 새 비밀번호 유효성 검증은 @Pattern으로 DTO에서 처리됨
            user.updatePassword(passwordEncoder.encode(request.newPassword()));
        }

        return new UpdateUserResponse(user.getUserId(), user.getNickname());
    }

    /**
     * U-3. 회원 탈퇴 — plan.md 3-4 U-3
     * - 비밀번호 검증 → status=DELETED (소프트 삭제) → refreshToken null → 쿠키 삭제
     */
    @Transactional
    public void deleteMe(Long userId, DeleteUserRequest request, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPasswordH())) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }

        // 소프트 삭제 (status=DELETED, refreshToken=null)
        user.delete();

        // 쿠키 삭제
        cookieUtil.deleteRefreshTokenCookie(response);
    }
}
