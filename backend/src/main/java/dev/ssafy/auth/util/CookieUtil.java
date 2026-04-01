package dev.ssafy.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * plan.md 3-3 Refresh Token 전달 방식 기준
 * Set-Cookie: HttpOnly, SameSite=Strict
 */
@Component
public class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        // SameSite=Strict 설정 (Cookie API 미지원 → Set-Cookie 헤더로 직접 추가)
        String cookieValue = REFRESH_TOKEN_COOKIE_NAME + "=" + refreshToken
                + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + maxAge;
        response.addHeader("Set-Cookie", cookieValue);
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        // plan.md 3-4 A-3: Max-Age=0으로 쿠키 삭제
        String cookieValue = REFRESH_TOKEN_COOKIE_NAME + "=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0";
        response.addHeader("Set-Cookie", cookieValue);
    }
}
