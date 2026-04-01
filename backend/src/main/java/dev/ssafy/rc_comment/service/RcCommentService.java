package dev.ssafy.rc_comment.service;

import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.rc_comment.dto.CommentItemDTO;
import dev.ssafy.rc_comment.dto.RcCommentCreateRequest;
import dev.ssafy.rc_comment.dto.RcCommentUpdateRequest;
import dev.ssafy.rc_comment.entity.RcComment;
import dev.ssafy.rc_comment.repository.RcCommentRepository;
import dev.ssafy.rcc_feedback.entity.RccFeedback;
import dev.ssafy.rcc_feedback.repository.RccFeedbackRepository;
import dev.ssafy.recommendation.dto.AuthorDTO;
import dev.ssafy.recommendation.dto.FeedbackSummaryDTO;
import dev.ssafy.recommendation.entity.Recommendation;
import dev.ssafy.recommendation.repository.RecommendationRepository;
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

/**
 * plan.md Phase 3 기준
 * RC-1 ~ RC-4 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RcCommentService {

    private final RcCommentRepository rcCommentRepository;
    private final RecommendationRepository recommendationRepository;
    private final RccFeedbackRepository rccFeedbackRepository;
    private final UserRepository userRepository;

    /**
     * RC-1: 추천 댓글 전체 조회 (createdAt ASC, 페이지네이션)
     */
    public Page<CommentItemDTO> getComments(Long rcId, int page, int size, Long currentUserId) {
        Recommendation recommendation = recommendationRepository.findById(rcId)
                .orElseThrow(() -> new EntityNotFoundException("추천 게시글을 찾을 수 없습니다."));

        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<RcComment> comments = rcCommentRepository.findByRecommendation(recommendation, pageable);

        return comments.map(c -> toCommentItemDTO(c, currentUser));
    }

    /**
     * RC-2: 추천 댓글 작성 (USER만 가능)
     */
    @Transactional
    public Long createComment(Long rcId, RcCommentCreateRequest request, Long userId, User.Role role) {


        Recommendation recommendation = recommendationRepository.findById(rcId)
                .orElseThrow(() -> new EntityNotFoundException("추천 게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        RcComment comment = RcComment.builder()
                .recommendation(recommendation)
                .user(user)
                .content(request.getContent())
                .build();

        return rcCommentRepository.save(comment).getRccId();
    }

    /**
     * RC-3: 추천 댓글 수정 (본인만)
     * [미정 사항] URL의 rcId와 댓글의 rc_id 일치 여부 검증: 미검증 (plan.md 7번)
     */
    @Transactional
    public Long updateComment(Long rccId, RcCommentUpdateRequest request, Long userId) {
        RcComment comment = rcCommentRepository.findById(rccId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 댓글만 수정할 수 있습니다.");
        }

        comment.update(request.getContent());
        return comment.getRccId();
    }

    /**
     * RC-4: 추천 댓글 삭제 (본인만, hard delete)
     * 연관 rcc_feedback cascade: 서비스 레이어에서 수동 처리
     */
    @Transactional
    public void deleteComment(Long rccId, Long userId, User.Role role) {
        RcComment comment = rcCommentRepository.findById(rccId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUserId().equals(userId) && role != User.Role.ADMIN) {
            throw new AccessDeniedException("본인의 댓글만 삭제할 수 있습니다.");
        }

        List<RccFeedback> feedbacks = rccFeedbackRepository.findAllByRcComment(comment);
        rccFeedbackRepository.deleteAll(feedbacks);

        rcCommentRepository.delete(comment);
    }

    // ---- Helper ----

    private CommentItemDTO toCommentItemDTO(RcComment comment, User currentUser) {
        AuthorDTO author = new AuthorDTO(comment.getUser().getUserId(), comment.getUser().getNickname());
        long likeCount = rccFeedbackRepository.countByRcCommentAndStatus(comment, RccFeedback.FeedbackStatus.LIKE);
        long dislikeCount = rccFeedbackRepository.countByRcCommentAndStatus(comment, RccFeedback.FeedbackStatus.DISLIKE);
        String myFeedback = null;
        if (currentUser != null) {
            Optional<RccFeedback> existing = rccFeedbackRepository.findByUserAndRcComment(currentUser, comment);
            myFeedback = existing.map(f -> f.getStatus().name()).orElse(null);
        }
        FeedbackSummaryDTO feedbackSummary = new FeedbackSummaryDTO(likeCount, dislikeCount, myFeedback);

        return new CommentItemDTO(
                comment.getRccId(), comment.getContent(), author, feedbackSummary,
                comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }
}
