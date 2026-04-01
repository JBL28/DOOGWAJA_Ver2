package dev.ssafy.bs_comment.entity;

import dev.ssafy.bought_snack.entity.BoughtSnack;
import dev.ssafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * plan.md 3 기준
 * ERD: bs_comment 테이블
 */
@Entity
@Table(name = "bs_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BsComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bsc_id")
    private Long bscId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bs_id", nullable = false)
    private BoughtSnack boughtSnack;

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
    public BsComment(BoughtSnack boughtSnack, User user, String content) {
        this.boughtSnack = boughtSnack;
        this.user = user;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }
}
