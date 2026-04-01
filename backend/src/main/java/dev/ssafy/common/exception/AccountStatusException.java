package dev.ssafy.common.exception;

/**
 * 계정 상태 이상(비활성/탈퇴) 예외 — plan.md 3-7, api_spec.md 1-2
 */
public class AccountStatusException extends RuntimeException {
    public AccountStatusException(String message) {
        super(message);
    }
}
