package dev.ssafy.rcc_feedback.entity;

import dev.ssafy.rc_comment.entity.RcComment;
import dev.ssafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan.md 3-2 기준
 * ERD: rcc_feedback 테이블
 * UNIQUE 제약: (user_id, rcc_id)
 */
@Entity
@Table(
    name = "rcc_feedback",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "rcc_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RccFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rcc_id", nullable = false)
    private RcComment rcComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeedbackStatus status;

    @Builder
    public RccFeedback(RcComment rcComment, User user, FeedbackStatus status) {
        this.rcComment = rcComment;
        this.user = user;
        this.status = status;
    }

    public void updateStatus(FeedbackStatus status) {
        this.status = status;
    }

    public enum FeedbackStatus {
        LIKE, DISLIKE
    }
}
