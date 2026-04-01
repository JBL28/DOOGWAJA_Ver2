package dev.ssafy.rcc_feedback.repository;

import dev.ssafy.rc_comment.entity.RcComment;
import dev.ssafy.rcc_feedback.entity.RccFeedback;
import dev.ssafy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * plan.md Phase 1 기준
 */
public interface RccFeedbackRepository extends JpaRepository<RccFeedback, Long> {

    /**
     * 유저 + 댓글 기준 피드백 조회 (toggle 정책용)
     */
    Optional<RccFeedback> findByUserAndRcComment(User user, RcComment rcComment);

    /**
     * 댓글별 상태 카운트
     */
    long countByRcCommentAndStatus(RcComment rcComment, RccFeedback.FeedbackStatus status);

    /**
     * 댓글 삭제 시 cascade용
     */
    List<RccFeedback> findAllByRcComment(RcComment rcComment);
}
