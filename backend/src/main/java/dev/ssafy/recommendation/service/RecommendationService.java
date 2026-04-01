package dev.ssafy.recommendation.service;

import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.rc_comment.entity.RcComment;
import dev.ssafy.rc_comment.repository.RcCommentRepository;
import dev.ssafy.rc_feedback.entity.RcFeedback;
import dev.ssafy.rc_feedback.repository.RcFeedbackRepository;
import dev.ssafy.rcc_feedback.entity.RccFeedback;
import dev.ssafy.rcc_feedback.repository.RccFeedbackRepository;
import dev.ssafy.recommendation.dto.*;
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
import java.util.stream.Collectors;

/**
 * plan.md Phase 3 기준
 * R-1 ~ R-5 구현
 *
 * [미정 사항 처리] 추천글 목록 정렬 기준 (plan.md 7번):
 * API 명세에 명시 없음 → createdAt DESC (최신순)으로 구현
 * 추후 정책 확정 시 이 부분 변경 필요
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final RcCommentRepository rcCommentRepository;
    private final RcFeedbackRepository rcFeedbackRepository;
    private final RccFeedbackRepository rccFeedbackRepository;
    private final UserRepository userRepository;

    /**
     * R-1: 추천글 목록 조회
     * [미정 사항] 정렬 기준: createdAt DESC (최신순) — 명세에 없음, 추후 확인 필요
     */
    public Page<RecommendationListItemDTO> getRecommendations(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Recommendation> recommendations = recommendationRepository.findAll(pageable);

        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        return recommendations.map(rc -> toListItemDTO(rc, currentUser));
    }

    /**
     * R-2: 추천글 단건 조회
     */
    public RecommendationDetailDTO getRecommendation(Long rcId, Long currentUserId) {
        Recommendation recommendation = recommendationRepository.findById(rcId)
                .orElseThrow(() -> new EntityNotFoundException("추천 게시글을 찾을 수 없습니다."));

        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        return toDetailDTO(recommendation, currentUser);
    }

    /**
     * R-3: 추천글 생성 (USER만 가능)
     */
    @Transactional
    public Long createRecommendation(RecommendationCreateRequest request, Long userId, User.Role role) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Recommendation recommendation = Recommendation.builder()
                .user(user)
                .name(request.getName())
                .reason(request.getReason())
                .build();

        return recommendationRepository.save(recommendation).getRcId();
    }

    /**
     * R-4: 추천글 수정 (본인만)
     */
    @Transactional
    public Long updateRecommendation(Long rcId, RecommendationUpdateRequest request, Long userId) {
        Recommendation recommendation = recommendationRepository.findById(rcId)
                .orElseThrow(() -> new EntityNotFoundException("추천 게시글을 찾을 수 없습니다."));

        if (!recommendation.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 게시글만 수정할 수 있습니다.");
        }

        recommendation.update(request.getName(), request.getReason());
        return recommendation.getRcId();
    }

    /**
     * R-5: 추천글 삭제 (본인만, hard delete)
     * 연관 데이터(rc_comment, rc_feedback) cascade: 서비스 레이어에서 수동 처리
     */
    @Transactional
    public void deleteRecommendation(Long rcId, Long userId, User.Role role) {
        Recommendation recommendation = recommendationRepository.findById(rcId)
                .orElseThrow(() -> new EntityNotFoundException("추천 게시글을 찾을 수 없습니다."));

        if (!recommendation.getUser().getUserId().equals(userId) && role != User.Role.ADMIN) {
            throw new AccessDeniedException("본인의 게시글만 삭제할 수 있습니다.");
        }

        // 댓글 피드백 삭제 → 댓글 삭제 → 게시글 피드백 삭제 → 게시글 삭제
        List<RcComment> comments = rcCommentRepository.findByRecommendation(recommendation, Sort.unsorted());
        for (RcComment comment : comments) {
            List<dev.ssafy.rcc_feedback.entity.RccFeedback> rccFeedbacks =
                    rccFeedbackRepository.findAllByRcComment(comment);
            rccFeedbackRepository.deleteAll(rccFeedbacks);
        }
        rcCommentRepository.deleteAll(comments);

        List<RcFeedback> rcFeedbacks = rcFeedbackRepository.findAllByRecommendation(recommendation);
        rcFeedbackRepository.deleteAll(rcFeedbacks);

        recommendationRepository.delete(recommendation);
    }

    // ---- Helper: DTO 변환 ----

    private RecommendationListItemDTO toListItemDTO(Recommendation rc, User currentUser) {
        AuthorDTO author = new AuthorDTO(rc.getUser().getUserId(), rc.getUser().getNickname());
        FeedbackSummaryDTO feedbackSummary = buildFeedbackSummary(rc, currentUser);
        long commentCount = rcCommentRepository.countByRecommendation(rc);
        List<CommentPreviewItemDTO> commentPreview = buildCommentPreview(rc, currentUser);

        return new RecommendationListItemDTO(
                rc.getRcId(), rc.getName(), rc.getReason(),
                author, feedbackSummary, commentCount, commentPreview,
                rc.getCreatedAt(), rc.getUpdatedAt()
        );
    }

    private RecommendationDetailDTO toDetailDTO(Recommendation rc, User currentUser) {
        AuthorDTO author = new AuthorDTO(rc.getUser().getUserId(), rc.getUser().getNickname());
        FeedbackSummaryDTO feedbackSummary = buildFeedbackSummary(rc, currentUser);
        long commentCount = rcCommentRepository.countByRecommendation(rc);
        List<CommentPreviewItemDTO> commentPreview = buildCommentPreview(rc, currentUser);

        return new RecommendationDetailDTO(
                rc.getRcId(), rc.getName(), rc.getReason(),
                author, feedbackSummary, commentCount, commentPreview,
                rc.getCreatedAt(), rc.getUpdatedAt()
        );
    }

    private FeedbackSummaryDTO buildFeedbackSummary(Recommendation rc, User currentUser) {
        long likeCount = rcFeedbackRepository.countByRecommendationAndStatus(rc, RcFeedback.FeedbackStatus.LIKE);
        long dislikeCount = rcFeedbackRepository.countByRecommendationAndStatus(rc, RcFeedback.FeedbackStatus.DISLIKE);
        String myFeedback = null;
        if (currentUser != null) {
            Optional<RcFeedback> existing = rcFeedbackRepository.findByUserAndRecommendation(currentUser, rc);
            myFeedback = existing.map(f -> f.getStatus().name()).orElse(null);
        }
        return new FeedbackSummaryDTO(likeCount, dislikeCount, myFeedback);
    }

    /**
     * commentPreview: 최대 3개, 최신순 (createdAt DESC)
     * plan.md R-1, R-2 정책
     */
    private List<CommentPreviewItemDTO> buildCommentPreview(Recommendation rc, User currentUser) {
        List<RcComment> comments = rcCommentRepository.findByRecommendation(
                rc, Sort.by(Sort.Direction.DESC, "createdAt"));
        return comments.stream()
                .limit(3)
                .map(c -> toCommentPreviewItemDTO(c, currentUser))
                .collect(Collectors.toList());
    }

    private CommentPreviewItemDTO toCommentPreviewItemDTO(RcComment comment, User currentUser) {
        AuthorDTO author = new AuthorDTO(comment.getUser().getUserId(), comment.getUser().getNickname());
        long likeCount = rccFeedbackRepository.countByRcCommentAndStatus(comment, RccFeedback.FeedbackStatus.LIKE);
        long dislikeCount = rccFeedbackRepository.countByRcCommentAndStatus(comment, RccFeedback.FeedbackStatus.DISLIKE);
        String myFeedback = null;
        if (currentUser != null) {
            Optional<RccFeedback> existing = rccFeedbackRepository.findByUserAndRcComment(currentUser, comment);
            myFeedback = existing.map(f -> f.getStatus().name()).orElse(null);
        }
        FeedbackSummaryDTO feedbackSummary = new FeedbackSummaryDTO(likeCount, dislikeCount, myFeedback);

        return new CommentPreviewItemDTO(
                comment.getRccId(), comment.getContent(), author, feedbackSummary,
                comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }
}
