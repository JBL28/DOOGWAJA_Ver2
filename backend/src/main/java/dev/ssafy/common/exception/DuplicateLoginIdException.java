package dev.ssafy.common.exception;

/**
 * 중복 loginId 예외 — plan.md 3-7, api_spec.md 1-1
 */
public class DuplicateLoginIdException extends RuntimeException {
    public DuplicateLoginIdException(String message) {
        super(message);
    }
}
