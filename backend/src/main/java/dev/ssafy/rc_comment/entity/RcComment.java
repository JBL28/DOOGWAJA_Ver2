package dev.ssafy.rc_comment.entity;

import dev.ssafy.recommendation.entity.Recommendation;
import dev.ssafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * plan.md 3-2 기준
 * ERD: rc_comment 테이블
 */
@Entity
@Table(name = "rc_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RcComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rcc_id")
    private Long rccId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rc_id", nullable = false)
    private Recommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

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
    public RcComment(Recommendation recommendation, User user, String content) {
        this.recommendation = recommendation;
        this.user = user;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }
}
