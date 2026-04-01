package dev.ssafy.common.exception;

/**
 * 엔티티 없음 예외 — plan.md 3-7
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
