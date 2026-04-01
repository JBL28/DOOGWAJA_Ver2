package dev.ssafy.common.exception;

/**
 * 비밀번호 불일치 예외 — plan.md 3-7, api_spec.md 2-2, 2-3
 */
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) {
        super(message);
    }
}
