package dev.ssafy.auth.service;

import dev.ssafy.auth.dto.LoginRequest;
import dev.ssafy.auth.dto.LoginResponse;
import dev.ssafy.auth.dto.RegisterRequest;
import dev.ssafy.auth.dto.TokenResponse;
import dev.ssafy.auth.util.CookieUtil;
import dev.ssafy.auth.util.JwtProvider;
import dev.ssafy.common.exception.AccountStatusException;
import dev.ssafy.common.exception.DuplicateLoginIdException;
import dev.ssafy.common.exception.InvalidCredentialsException;
import dev.ssafy.common.exception.InvalidTokenException;
import dev.ssafy.user.entity.User;
import dev.ssafy.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * plan.md Phase 3 Step 10 기준
 * A-1 register / A-2 login / A-3 logout / A-4 refresh
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    private static final Logger BIZ_LOG = LoggerFactory.getLogger("BUSINESS_LOG");

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * A-1. 회원가입 — plan.md 3-4 A-1
     */
    @Transactional
    public void register(RegisterRequest request) {
        // loginId 중복 확인 → 중복 시 409
        if (userRepository.existsByLoginId(request.loginId())) {
            BIZ_LOG.info("{\"event\":\"register_failed\", \"reason\":\"duplicate_login_id\", \"loginId\":\"{}\"}", request.loginId());
            throw new DuplicateLoginIdException("이미 사용 중인 아이디입니다.");
        }

        User user = User.builder()
                .loginId(request.loginId())
                .passwordH(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(User.Role.USER)
                .status(User.Status.ACTIVATED)
                .build();

        userRepository.save(user);
        BIZ_LOG.info("{\"event\":\"register_success\", \"loginId\":\"{}\"}", user.getLoginId());
    }

    /**
     * A-2. 로그인 — plan.md 3-4 A-2
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        // loginId로 유저 조회 → 없으면 401
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> {
                    BIZ_LOG.info("{\"event\":\"login_failed\", \"reason\":\"user_not_found\", \"loginId\":\"{}\"}", request.loginId());
                    return new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
                });

        // 계정 상태 확인 (비활성/탈퇴) → 403
        if (user.getStatus() == User.Status.DEACTIVATED) {
            BIZ_LOG.info("{\"event\":\"login_failed\", \"reason\":\"deactivated_account\", \"loginId\":\"{}\"}", request.loginId());
            throw new AccountStatusException("비활성화된 계정입니다.");
        }
        if (user.getStatus() == User.Status.DELETED) {
            BIZ_LOG.info("{\"event\":\"login_failed\", \"reason\":\"deleted_account\", \"loginId\":\"{}\"}", request.loginId());
            throw new AccountStatusException("탈퇴한 계정입니다.");
        }

        // 비밀번호 검증 → 불일치 시 401
        if (!passwordEncoder.matches(request.password(), user.getPasswordH())) {
            BIZ_LOG.info("{\"event\":\"login_failed\", \"reason\":\"password_mismatch\", \"loginId\":\"{}\"}", request.loginId());
            throw new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(user.getUserId(), user.getRole().name());
        
        BIZ_LOG.info("{\"event\":\"login_success\", \"userId\":{}, \"loginId\":\"{}\"}", user.getUserId(), user.getLoginId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        // Refresh Token DB 저장
        user.updateRefreshToken(refreshToken);

        // Refresh Token Set-Cookie (HttpOnly, SameSite=Strict) — plan.md 3-3
        int maxAgeSec = (int) (refreshExpiration / 1000);
        cookieUtil.addRefreshTokenCookie(response, refreshToken, maxAgeSec);

        return new LoginResponse(
                accessToken,
                user.getUserId(),
                user.getLoginId(),
                user.getNickname(),
                user.getRole().name()
        );
    }

    /**
     * A-3. 로그아웃 — plan.md 3-4 A-3
     */
    @Transactional
    public void logout(Long userId, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("사용자를 찾을 수 없습니다."));

        // DB refresh_token null 처리
        user.updateRefreshToken(null);

        // 쿠키 Max-Age=0
        cookieUtil.deleteRefreshTokenCookie(response);
    }

    /**
     * A-4. Access Token 재발급 — plan.md 3-4 A-4
     */
    @Transactional
    public TokenResponse refresh(HttpServletRequest request) {
        // 쿠키에서 refreshToken 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new InvalidTokenException("유효하지 않은 Refresh Token입니다.");
        }

        // JWT 서명 및 만료 검증 (JwtProvider에서 만료/유효하지 않음 구분)
        Long userId = jwtProvider.getUserId(refreshToken); // 만료/유효하지 않으면 예외 발생

        // DB 값과 일치 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 Refresh Token입니다."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException("유효하지 않은 Refresh Token입니다.");
        }

        // 새 Access Token 발급
        String newAccessToken = jwtProvider.generateAccessToken(user.getUserId(), user.getRole().name());
        return new TokenResponse(newAccessToken);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
