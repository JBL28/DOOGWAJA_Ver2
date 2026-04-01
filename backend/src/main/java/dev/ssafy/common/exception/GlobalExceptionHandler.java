package dev.ssafy.common.exception;

import dev.ssafy.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * plan.md 3-7 기준 전역 예외 처리
 * plan.md 3-6: AccessDeniedException (403), EntityNotFoundException (404) 추가
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("입력값이 유효하지 않습니다.");
        log.info("입력 검증 실패: {}", message);
        return ResponseEntity.badRequest().body(ApiResponse.failure(message));
    }

    @ExceptionHandler(DuplicateLoginIdException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateLoginId(DuplicateLoginIdException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException e) {
        log.warn("인증 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountStatus(AccountStatusException e) {
        log.warn("계정 상태 이상 접근: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException e) {
        log.warn("유효하지 않은 토큰: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordMismatch(PasswordMismatchException e) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException e) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("존재하지 않는 리소스 조회: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(e.getMessage()));
    }

    /**
     * plan.md 3-6: 403 처리
     * - 본인 아닌 수정/삭제 시도
     * - ADMIN이 USER 전용 기능 호출 시
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("권한 부족 접근 시도: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(e.getMessage()));
    }

    /**
     * plan.md 3-6: 잘못된 피드백 status 값 입력 시 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.info("잘못된 파라미터 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
    }

    /**
     * 예기치 못한 전체 에러(500) 핸들러 (CORS 헤더 유실 방지 및 로깅)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception e) {
        log.error("서버 내부 오류 발생: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("서버 내부 오류가 발생했습니다: " + e.getMessage()));
    }
}

