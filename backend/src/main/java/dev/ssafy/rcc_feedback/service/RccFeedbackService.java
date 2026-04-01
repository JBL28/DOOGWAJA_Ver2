package dev.ssafy.rcc_feedback.service;

import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.rc_comment.entity.RcComment;
import dev.ssafy.rc_comment.repository.RcCommentRepository;
import dev.ssafy.rc_feedback.dto.FeedbackRequest;
import dev.ssafy.rc_feedback.dto.FeedbackResultDTO;
import dev.ssafy.rcc_feedback.entity.RccFeedback;
import dev.ssafy.rcc_feedback.repository.RccFeedbackRepository;
import dev.ssafy.user.entity.User;
import dev.ssafy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * plan.md RCF-1 기준
 * 추천 댓글 피드백 toggle 구현
 *
 * Toggle 정책: RF-1과 동일 (대상이 댓글)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RccFeedbackService {

    private final RccFeedbackRepository rccFeedbackRepository;
    private final RcCommentRepository rcCommentRepository;
    private final UserRepository userRepository;

    /**
     * RCF-1: 추천 댓글 피드백 toggle (USER만 가능)
     */
    public FeedbackResultDTO toggleFeedback(Long rccId, FeedbackRequest request, Long userId, User.Role role) {


        RcComment comment = rcCommentRepository.findById(rccId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        RccFeedback.FeedbackStatus requestedStatus = parseStatus(request.getStatus());

        Optional<RccFeedback> existing = rccFeedbackRepository.findByUserAndRcComment(user, comment);

        String myFeedback;
        if (existing.isEmpty()) {
            RccFeedback feedback = RccFeedback.builder()
                    .rcComment(comment)
                    .user(user)
                    .status(requestedStatus)
                    .build();
            rccFeedbackRepository.save(feedback);
            myFeedback = requestedStatus.name();
        } else {
            RccFeedback feedback = existing.get();
            if (feedback.getStatus() == requestedStatus) {
                rccFeedbackRepository.delete(feedback);
                myFeedback = null;
            } else {
                feedback.updateStatus(requestedStatus);
                myFeedback = requestedStatus.name();
            }
        }

        long likeCount = rccFeedbackRepository.countByRcCommentAndStatus(comment, RccFeedback.FeedbackStatus.LIKE);
        long dislikeCount = rccFeedbackRepository.countByRcCommentAndStatus(comment, RccFeedback.FeedbackStatus.DISLIKE);

        return new FeedbackResultDTO(myFeedback, likeCount, dislikeCount);
    }

    private RccFeedback.FeedbackStatus parseStatus(String status) {
        try {
            return RccFeedback.FeedbackStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 피드백 상태값입니다. LIKE 또는 DISLIKE만 허용됩니다.");
        }
    }
}
