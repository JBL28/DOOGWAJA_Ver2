package dev.ssafy.rc_feedback.repository;

import dev.ssafy.rc_feedback.entity.RcFeedback;
import dev.ssafy.recommendation.entity.Recommendation;
import dev.ssafy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * plan.md Phase 1 기준
 */
public interface RcFeedbackRepository extends JpaRepository<RcFeedback, Long> {

    /**
     * 유저 + 추천글 기준 피드백 조회 (toggle 정책용)
     */
    Optional<RcFeedback> findByUserAndRecommendation(User user, Recommendation recommendation);

    /**
     * 추천글별 상태 카운트
     */
    long countByRecommendationAndStatus(Recommendation recommendation, RcFeedback.FeedbackStatus status);

    /**
     * 추천글 삭제 시 cascade용
     */
    List<RcFeedback> findAllByRecommendation(Recommendation recommendation);
}
