package dev.ssafy.bsc_feedback.repository;

import dev.ssafy.bs_comment.entity.BsComment;
import dev.ssafy.bsc_feedback.entity.BscFeedback;
import dev.ssafy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BscFeedbackRepository extends JpaRepository<BscFeedback, Long> {

    /**
     * 유저 + 댓글 기준 피드백 조회 (toggle 정책용)
     */
    Optional<BscFeedback> findByUserAndBsComment(User user, BsComment bsComment);

    /**
     * 댓글별 상태 카운트
     */
    long countByBsCommentAndStatus(BsComment bsComment, BscFeedback.FeedbackStatus status);

    /**
     * 댓글 삭제 시 cascade용
     */
    List<BscFeedback> findAllByBsComment(BsComment bsComment);
}
