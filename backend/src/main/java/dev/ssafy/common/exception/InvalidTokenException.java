package dev.ssafy.common.exception;

/**
 * JWT 토큰 유효하지 않음 예외 — plan.md 3-7, api_spec.md 1-4
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
