package dev.ssafy.bs_feedback.repository;

import dev.ssafy.bought_snack.entity.BoughtSnack;
import dev.ssafy.bs_feedback.entity.BsFeedback;
import dev.ssafy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BsFeedbackRepository extends JpaRepository<BsFeedback, Long> {

    /**
     * 유저 + 과자 기준 피드백 조회 (toggle 정책용)
     */
    Optional<BsFeedback> findByUserAndBoughtSnack(User user, BoughtSnack boughtSnack);

    /**
     * 과자별 상태 카운트
     */
    long countByBoughtSnackAndStatus(BoughtSnack boughtSnack, BsFeedback.FeedbackStatus status);

    /**
     * 과자 삭제 시 cascade용
     */
    List<BsFeedback> findAllByBoughtSnack(BoughtSnack boughtSnack);

    /**
     * 좋아요 표시한 유저 목록 조회 (Top 21)
     */
    List<BsFeedback> findTop21ByBoughtSnackAndStatusOrderByIdDesc(BoughtSnack boughtSnack, BsFeedback.FeedbackStatus status);
}
