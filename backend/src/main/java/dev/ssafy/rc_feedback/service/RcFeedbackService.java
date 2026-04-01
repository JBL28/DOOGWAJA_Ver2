package dev.ssafy.rc_feedback.service;

import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.rc_feedback.dto.FeedbackRequest;
import dev.ssafy.rc_feedback.dto.FeedbackResultDTO;
import dev.ssafy.rc_feedback.entity.RcFeedback;
import dev.ssafy.rc_feedback.repository.RcFeedbackRepository;
import dev.ssafy.recommendation.entity.Recommendation;
import dev.ssafy.recommendation.repository.RecommendationRepository;
import dev.ssafy.user.entity.User;
import dev.ssafy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * plan.md RF-1 기준
 * 추천글 피드백 toggle 구현
 *
 * Toggle 정책 (plan.md 3-4):
 * - 없음 + LIKE → LIKE 생성
 * - LIKE + LIKE → 삭제 (toggle off)
 * - LIKE + DISLIKE → DISLIKE로 변경
 * - DISLIKE + DISLIKE → 삭제 (toggle off)
 * - DISLIKE + LIKE → LIKE로 변경
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RcFeedbackService {

    private final RcFeedbackRepository rcFeedbackRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;

    /**
     * RF-1: 추천글 피드백 toggle (USER만 가능)
     */
    public FeedbackResultDTO toggleFeedback(Long rcId, FeedbackRequest request, Long userId, User.Role role) {


        Recommendation recommendation = recommendationRepository.findById(rcId)
                .orElseThrow(() -> new EntityNotFoundException("추천 게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        RcFeedback.FeedbackStatus requestedStatus = parseStatus(request.getStatus());

        Optional<RcFeedback> existing = rcFeedbackRepository.findByUserAndRecommendation(user, recommendation);

        String myFeedback;
        if (existing.isEmpty()) {
            // 없음 → 생성
            RcFeedback feedback = RcFeedback.builder()
                    .recommendation(recommendation)
                    .user(user)
                    .status(requestedStatus)
                    .build();
            rcFeedbackRepository.save(feedback);
            myFeedback = requestedStatus.name();
        } else {
            RcFeedback feedback = existing.get();
            if (feedback.getStatus() == requestedStatus) {
                // 동일 상태 → 삭제 (toggle off)
                rcFeedbackRepository.delete(feedback);
                myFeedback = null;
            } else {
                // 반대 상태 → 변경
                feedback.updateStatus(requestedStatus);
                myFeedback = requestedStatus.name();
            }
        }

        long likeCount = rcFeedbackRepository.countByRecommendationAndStatus(recommendation, RcFeedback.FeedbackStatus.LIKE);
        long dislikeCount = rcFeedbackRepository.countByRecommendationAndStatus(recommendation, RcFeedback.FeedbackStatus.DISLIKE);

        return new FeedbackResultDTO(myFeedback, likeCount, dislikeCount);
    }

    private RcFeedback.FeedbackStatus parseStatus(String status) {
        try {
            return RcFeedback.FeedbackStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 피드백 상태값입니다. LIKE 또는 DISLIKE만 허용됩니다.");
        }
    }
}
