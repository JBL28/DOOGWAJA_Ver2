package dev.ssafy.bs_feedback.entity;

import dev.ssafy.bought_snack.entity.BoughtSnack;
import dev.ssafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan.md 4 기준
 * ERD: bs_feedback 테이블
 */
@Entity
@Table(
    name = "bs_feedback",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "bs_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BsFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bs_id", nullable = false)
    private BoughtSnack boughtSnack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeedbackStatus status;

    @Builder
    public BsFeedback(BoughtSnack boughtSnack, User user, FeedbackStatus status) {
        this.boughtSnack = boughtSnack;
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
