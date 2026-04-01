package dev.ssafy.bought_snack.service;

import dev.ssafy.bought_snack.dto.*;
import dev.ssafy.bought_snack.entity.BoughtSnack;
import dev.ssafy.bought_snack.repository.BoughtSnackRepository;
import dev.ssafy.bs_comment.entity.BsComment;
import dev.ssafy.bs_comment.repository.BsCommentRepository;
import dev.ssafy.bs_feedback.entity.BsFeedback;
import dev.ssafy.bs_feedback.repository.BsFeedbackRepository;
import dev.ssafy.bsc_feedback.entity.BscFeedback;
import dev.ssafy.bsc_feedback.repository.BscFeedbackRepository;
import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.recommendation.dto.AuthorDTO;
import dev.ssafy.recommendation.dto.CommentPreviewItemDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoughtSnackService {

    private final BoughtSnackRepository boughtSnackRepository;
    private final BsCommentRepository bsCommentRepository;
    private final BsFeedbackRepository bsFeedbackRepository;
    private final BscFeedbackRepository bscFeedbackRepository;
    private final UserRepository userRepository;

    private static final Logger BIZ_LOG = LoggerFactory.getLogger("BUSINESS_LOG");

    /**
     * 7-1 구매 과자 목록 조회
     */
    public Page<BoughtSnackListItemDTO> getBoughtSnacks(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BoughtSnack> boughtSnacks = boughtSnackRepository.findAll(pageable);

        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        return boughtSnacks.map(bs -> toListItemDTO(bs, currentUser));
    }

    /**
     * 7-2 구매 과자 단건 조회
     */
    public BoughtSnackDetailDTO getBoughtSnack(Long bsId, Long currentUserId) {
        BoughtSnack boughtSnack = boughtSnackRepository.findById(bsId)
                .orElseThrow(() -> new EntityNotFoundException("구매 과자를 찾을 수 없습니다."));

        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        return toDetailDTO(boughtSnack, currentUser);
    }

    /**
     * 7-3 구매 과자 등록 (ADMIN만)
     */
    @Transactional
    public Long createBoughtSnack(BoughtSnackCreateRequest request, User.Role role) {
        if (role != User.Role.ADMIN) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }

        BoughtSnack boughtSnack = BoughtSnack.builder()
                .name(request.getName())
                // .status is automatically SHIPPING if not specified, by PrePersist/Builder logic
                .build();

        boughtSnack = boughtSnackRepository.save(boughtSnack);
        BIZ_LOG.info("{\"event\":\"bought_snack_created\", \"bsId\":{}, \"name\":\"{}\"}", boughtSnack.getBsId(), boughtSnack.getName());
        return boughtSnack.getBsId();
    }

    /**
     * 7-4 구매 과자 수정 (ADMIN만)
     */
    @Transactional
    public Long updateBoughtSnack(Long bsId, BoughtSnackUpdateRequest request, User.Role role) {
        if (role != User.Role.ADMIN) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }

        BoughtSnack boughtSnack = boughtSnackRepository.findById(bsId)
                .orElseThrow(() -> new EntityNotFoundException("구매 과자를 찾을 수 없습니다."));

        boughtSnack.update(request.getName());
        return boughtSnack.getBsId();
    }

    /**
     * 7-5 구매 과자 삭제 (ADMIN만, hard delete)
     */
    @Transactional
    public void deleteBoughtSnack(Long bsId, User.Role role) {
        if (role != User.Role.ADMIN) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }

        BoughtSnack boughtSnack = boughtSnackRepository.findById(bsId)
                .orElseThrow(() -> new EntityNotFoundException("구매 과자를 찾을 수 없습니다."));

        // cascade delete
        List<BsComment> comments = bsCommentRepository.findByBoughtSnack(boughtSnack, Sort.unsorted());
        for (BsComment comment : comments) {
            List<BscFeedback> bscFeedbacks = bscFeedbackRepository.findAllByBsComment(comment);
            bscFeedbackRepository.deleteAll(bscFeedbacks);
        }
        bsCommentRepository.deleteAll(comments);

        List<BsFeedback> bsFeedbacks = bsFeedbackRepository.findAllByBoughtSnack(boughtSnack);
        bsFeedbackRepository.deleteAll(bsFeedbacks);

        boughtSnackRepository.delete(boughtSnack);
    }

    /**
     * 7-6 구매 과자 상태 변경 (USER, ADMIN 모두)
     */
    @Transactional
    public BoughtSnackDetailDTO updateBoughtSnackStatus(Long bsId, BoughtSnackStatusUpdateRequest request, Long currentUserId) {
        BoughtSnack boughtSnack = boughtSnackRepository.findById(bsId)
                .orElseThrow(() -> new EntityNotFoundException("구매 과자를 찾을 수 없습니다."));

        try {
            BoughtSnack.SnackStatus newStatus = BoughtSnack.SnackStatus.valueOf(request.getStatus());
            boughtSnack.updateStatus(newStatus);
            BIZ_LOG.info("{\"event\":\"bought_snack_status_updated\", \"bsId\":{}, \"newStatus\":\"{}\", \"userId\":{}}", bsId, request.getStatus(), currentUserId);
        } catch (IllegalArgumentException e) {
            BIZ_LOG.warn("{\"event\":\"bought_snack_status_update_failed\", \"reason\":\"invalid_status\", \"bsId\":{}, \"requestedStatus\":\"{}\", \"userId\":{}}", bsId, request.getStatus(), currentUserId);
            throw new dev.ssafy.common.exception.BadRequestException("유효하지 않은 상태 값입니다.");
        }

        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        return toDetailDTO(boughtSnack, currentUser);
    }


    // ---- Helper: DTO 변환 ----

    private BoughtSnackListItemDTO toListItemDTO(BoughtSnack bs, User currentUser) {
        FeedbackSummaryDTO feedbackSummary = buildFeedbackSummary(bs, currentUser);
        long commentCount = bsCommentRepository.countByBoughtSnack(bs);
        List<CommentPreviewItemDTO> commentPreview = buildCommentPreview(bs, currentUser);

        return new BoughtSnackListItemDTO(
                bs.getBsId(), bs.getName(),
                bs.getStatus().name(), bs.getStatus().getLabel(),
                feedbackSummary, commentCount, commentPreview,
                bs.getCreatedAt(), bs.getUpdatedAt()
        );
    }

    private BoughtSnackDetailDTO toDetailDTO(BoughtSnack bs, User currentUser) {
        FeedbackSummaryDTO feedbackSummary = buildFeedbackSummary(bs, currentUser);
        long commentCount = bsCommentRepository.countByBoughtSnack(bs);
        List<CommentPreviewItemDTO> commentPreview = buildCommentPreview(bs, currentUser);

        return new BoughtSnackDetailDTO(
                bs.getBsId(), bs.getName(),
                bs.getStatus().name(), bs.getStatus().getLabel(),
                feedbackSummary, commentCount, commentPreview,
                bs.getCreatedAt(), bs.getUpdatedAt()
        );
    }

    private FeedbackSummaryDTO buildFeedbackSummary(BoughtSnack bs, User currentUser) {
        long likeCount = bsFeedbackRepository.countByBoughtSnackAndStatus(bs, BsFeedback.FeedbackStatus.LIKE);
        long dislikeCount = bsFeedbackRepository.countByBoughtSnackAndStatus(bs, BsFeedback.FeedbackStatus.DISLIKE);
        String myFeedback = null;
        if (currentUser != null) {
            Optional<BsFeedback> existing = bsFeedbackRepository.findByUserAndBoughtSnack(currentUser, bs);
            myFeedback = existing.map(f -> f.getStatus().name()).orElse(null);
        }
        return new FeedbackSummaryDTO(likeCount, dislikeCount, myFeedback);
    }

    private List<CommentPreviewItemDTO> buildCommentPreview(BoughtSnack bs, User currentUser) {
        List<BsComment> comments = bsCommentRepository.findByBoughtSnack(
                bs, Sort.by(Sort.Direction.DESC, "createdAt"));
        return comments.stream()
                .limit(3)
                .map(c -> toCommentPreviewItemDTO(c, currentUser))
                .collect(Collectors.toList());
    }

    private CommentPreviewItemDTO toCommentPreviewItemDTO(BsComment comment, User currentUser) {
        AuthorDTO author = new AuthorDTO(comment.getUser().getUserId(), comment.getUser().getNickname());
        long likeCount = bscFeedbackRepository.countByBsCommentAndStatus(comment, BscFeedback.FeedbackStatus.LIKE);
        long dislikeCount = bscFeedbackRepository.countByBsCommentAndStatus(comment, BscFeedback.FeedbackStatus.DISLIKE);
        String myFeedback = null;
        if (currentUser != null) {
            Optional<BscFeedback> existing = bscFeedbackRepository.findByUserAndBsComment(currentUser, comment);
            myFeedback = existing.map(f -> f.getStatus().name()).orElse(null);
        }
        FeedbackSummaryDTO feedbackSummary = new FeedbackSummaryDTO(likeCount, dislikeCount, myFeedback);

        return new CommentPreviewItemDTO(
                comment.getBscId(), comment.getContent(), author, feedbackSummary,
                comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }
}
