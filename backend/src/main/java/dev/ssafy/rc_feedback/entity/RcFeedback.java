package dev.ssafy.rc_feedback.entity;

import dev.ssafy.recommendation.entity.Recommendation;
import dev.ssafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan.md 3-2 기준
 * ERD: rc_feedback 테이블
 * UNIQUE 제약: (user_id, rc_id)
 */
@Entity
@Table(
    name = "rc_feedback",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "rc_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RcFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rc_id", nullable = false)
    private Recommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeedbackStatus status;

    @Builder
    public RcFeedback(Recommendation recommendation, User user, FeedbackStatus status) {
        this.recommendation = recommendation;
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
