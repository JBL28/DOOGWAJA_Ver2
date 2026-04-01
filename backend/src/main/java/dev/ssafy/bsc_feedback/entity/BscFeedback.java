package dev.ssafy.bsc_feedback.entity;

import dev.ssafy.bs_comment.entity.BsComment;
import dev.ssafy.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * plan.md 4 기준
 * ERD: bsc_feedback 테이블
 */
@Entity
@Table(
    name = "bsc_feedback",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "bsc_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BscFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bsc_id", nullable = false)
    private BsComment bsComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeedbackStatus status;

    @Builder
    public BscFeedback(BsComment bsComment, User user, FeedbackStatus status) {
        this.bsComment = bsComment;
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
