package dev.ssafy.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ERD: user 테이블
 * plan.md 3-2 기준
 * refresh_token 컬럼 추가 (plan.md 3-3 DB 저장 정책)
 */
@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password_h", nullable = false, length = 255)
    private String passwordH;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public User(String loginId, String passwordH, String nickname, Role role, Status status) {
        this.loginId = loginId;
        this.passwordH = passwordH;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    // --- 도메인 메서드 ---

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String encodedPassword) {
        this.passwordH = encodedPassword;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void delete() {
        this.status = Status.DELETED;
        this.refreshToken = null;
    }

    // --- ENUM ---

    public enum Role {
        ADMIN, USER
    }

    public enum Status {
        ACTIVATED, DEACTIVATED, DELETED
    }
}
