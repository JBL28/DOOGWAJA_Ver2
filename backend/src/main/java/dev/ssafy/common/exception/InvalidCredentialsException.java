package dev.ssafy.common.exception;

/**
 * 아이디/비밀번호 불일치 예외 — plan.md 3-7, api_spec.md 1-2
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
