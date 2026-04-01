package dev.ssafy.bs_comment.service;

import dev.ssafy.bought_snack.entity.BoughtSnack;
import dev.ssafy.bought_snack.repository.BoughtSnackRepository;
import dev.ssafy.bs_comment.dto.BsCommentCreateRequest;
import dev.ssafy.bs_comment.dto.BsCommentUpdateRequest;
import dev.ssafy.bs_comment.entity.BsComment;
import dev.ssafy.bs_comment.repository.BsCommentRepository;
import dev.ssafy.bsc_feedback.entity.BscFeedback;
import dev.ssafy.bsc_feedback.repository.BscFeedbackRepository;
import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.rc_comment.dto.CommentItemDTO;
import dev.ssafy.recommendation.dto.AuthorDTO;
import dev.ssafy.recommendation.dto.FeedbackSummaryDTO;
import dev.ssafy.user.entity.User;
import dev.ssafy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BsCommentService {

    private final BsCommentRepository bsCommentRepository;
    private final BoughtSnackRepository boughtSnackRepository;
    private final BscFeedbackRepository bscFeedbackRepository;
    private final UserRepository userRepository;

    /**
     * 8-1 구매 과자 댓글 전체 조회
     */
    public Page<CommentItemDTO> getComments(Long bsId, int page, int size, Long currentUserId) {
        BoughtSnack boughtSnack = boughtSnackRepository.findById(bsId)
                .orElseThrow(() -> new EntityNotFoundException("구매 과자를 찾을 수 없습니다."));

        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<BsComment> comments = bsCommentRepository.findByBoughtSnack(boughtSnack, pageable);

        return comments.map(c -> toCommentItemDTO(c, currentUser));
    }

    /**
     * 8-2 구매 과자 댓글 작성 (USER만)
     */
    @Transactional
    public Long createComment(Long bsId, BsCommentCreateRequest request, Long userId, User.Role role) {

        BoughtSnack boughtSnack = boughtSnackRepository.findById(bsId)
                .orElseThrow(() -> new EntityNotFoundException("구매 과자를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        BsComment comment = BsComment.builder()
                .boughtSnack(boughtSnack)
                .user(user)
                .content(request.getContent())
                .build();

        return bsCommentRepository.save(comment).getBscId();
    }

    /**
     * 8-3 구매 과자 댓글 수정 (본인만)
     */
    @Transactional
    public Long updateComment(Long bscId, BsCommentUpdateRequest request, Long userId) {
        BsComment comment = bsCommentRepository.findById(bscId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 댓글만 수정할 수 있습니다.");
        }

        comment.update(request.getContent());
        return comment.getBscId();
    }

    /**
     * 8-4 구매 과자 댓글 삭제 (본인만)
     */
    @Transactional
    public void deleteComment(Long bscId, Long userId) {
        BsComment comment = bsCommentRepository.findById(bscId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 댓글만 삭제할 수 있습니다.");
        }

        List<BscFeedback> feedbacks = bscFeedbackRepository.findAllByBsComment(comment);
        bscFeedbackRepository.deleteAll(feedbacks);

        bsCommentRepository.delete(comment);
    }

    private CommentItemDTO toCommentItemDTO(BsComment comment, User currentUser) {
        AuthorDTO author = new AuthorDTO(comment.getUser().getUserId(), comment.getUser().getNickname());
        long likeCount = bscFeedbackRepository.countByBsCommentAndStatus(comment, BscFeedback.FeedbackStatus.LIKE);
        long dislikeCount = bscFeedbackRepository.countByBsCommentAndStatus(comment, BscFeedback.FeedbackStatus.DISLIKE);
        String myFeedback = null;
        if (currentUser != null) {
            Optional<BscFeedback> existing = bscFeedbackRepository.findByUserAndBsComment(currentUser, comment);
            myFeedback = existing.map(f -> f.getStatus().name()).orElse(null);
        }
        FeedbackSummaryDTO feedbackSummary = new FeedbackSummaryDTO(likeCount, dislikeCount, myFeedback);

        return new CommentItemDTO(
                comment.getBscId(), comment.getContent(), author, feedbackSummary,
                comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }
}
