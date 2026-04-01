package dev.ssafy.bsc_feedback.service;

import dev.ssafy.bs_comment.entity.BsComment;
import dev.ssafy.bs_comment.repository.BsCommentRepository;
import dev.ssafy.bsc_feedback.entity.BscFeedback;
import dev.ssafy.bsc_feedback.repository.BscFeedbackRepository;
import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.rc_feedback.dto.FeedbackRequest;
import dev.ssafy.rc_feedback.dto.FeedbackResultDTO;
import dev.ssafy.user.entity.User;
import dev.ssafy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BscFeedbackService {

    private final BscFeedbackRepository bscFeedbackRepository;
    private final BsCommentRepository bsCommentRepository;
    private final UserRepository userRepository;

    /**
     * 10-1 구매 과자 댓글 피드백 (USER만)
     */
    public FeedbackResultDTO toggleFeedback(Long bscId, FeedbackRequest request, Long userId, User.Role role) {

        BsComment comment = bsCommentRepository.findById(bscId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        BscFeedback.FeedbackStatus requestedStatus = parseStatus(request.getStatus());

        Optional<BscFeedback> existing = bscFeedbackRepository.findByUserAndBsComment(user, comment);

        String myFeedback;
        if (existing.isEmpty()) {
            BscFeedback feedback = BscFeedback.builder()
                    .bsComment(comment)
                    .user(user)
                    .status(requestedStatus)
                    .build();
            bscFeedbackRepository.save(feedback);
            myFeedback = requestedStatus.name();
        } else {
            BscFeedback feedback = existing.get();
            if (feedback.getStatus() == requestedStatus) {
                bscFeedbackRepository.delete(feedback);
                myFeedback = null;
            } else {
                feedback.updateStatus(requestedStatus);
                myFeedback = requestedStatus.name();
            }
        }

        long likeCount = bscFeedbackRepository.countByBsCommentAndStatus(comment, BscFeedback.FeedbackStatus.LIKE);
        long dislikeCount = bscFeedbackRepository.countByBsCommentAndStatus(comment, BscFeedback.FeedbackStatus.DISLIKE);

        return new FeedbackResultDTO(myFeedback, likeCount, dislikeCount);
    }

    private BscFeedback.FeedbackStatus parseStatus(String status) {
        try {
            return BscFeedback.FeedbackStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new dev.ssafy.common.exception.BadRequestException("잘못된 피드백 상태값입니다. LIKE 또는 DISLIKE만 허용됩니다.");
        }
    }
}
