package dev.ssafy.bs_feedback.service;

import dev.ssafy.bought_snack.entity.BoughtSnack;
import dev.ssafy.bought_snack.repository.BoughtSnackRepository;
import dev.ssafy.bs_feedback.entity.BsFeedback;
import dev.ssafy.bs_feedback.repository.BsFeedbackRepository;
import dev.ssafy.common.exception.EntityNotFoundException;
import dev.ssafy.rc_feedback.dto.FeedbackRequest;
import dev.ssafy.rc_feedback.dto.FeedbackResultDTO;
import dev.ssafy.user.entity.User;
import dev.ssafy.user.repository.UserRepository;
import dev.ssafy.common.dto.LikedUsersResponseDTO;
import dev.ssafy.user.dto.UserNicknameDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BsFeedbackService {

    private final BsFeedbackRepository bsFeedbackRepository;
    private final BoughtSnackRepository boughtSnackRepository;
    private final UserRepository userRepository;

    /**
     * 9-1 구매 과자 피드백 (USER만)
     */
    public FeedbackResultDTO toggleFeedback(Long bsId, FeedbackRequest request, Long userId, User.Role role) {

        BoughtSnack boughtSnack = boughtSnackRepository.findById(bsId)
                .orElseThrow(() -> new EntityNotFoundException("구매 과자를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        BsFeedback.FeedbackStatus requestedStatus = parseStatus(request.getStatus());

        Optional<BsFeedback> existing = bsFeedbackRepository.findByUserAndBoughtSnack(user, boughtSnack);

        String myFeedback;
        if (existing.isEmpty()) {
            BsFeedback feedback = BsFeedback.builder()
                    .boughtSnack(boughtSnack)
                    .user(user)
                    .status(requestedStatus)
                    .build();
            bsFeedbackRepository.save(feedback);
            myFeedback = requestedStatus.name();
        } else {
            BsFeedback feedback = existing.get();
            if (feedback.getStatus() == requestedStatus) {
                bsFeedbackRepository.delete(feedback);
                myFeedback = null;
            } else {
                feedback.updateStatus(requestedStatus);
                myFeedback = requestedStatus.name();
            }
        }

        long likeCount = bsFeedbackRepository.countByBoughtSnackAndStatus(boughtSnack, BsFeedback.FeedbackStatus.LIKE);
        long dislikeCount = bsFeedbackRepository.countByBoughtSnackAndStatus(boughtSnack, BsFeedback.FeedbackStatus.DISLIKE);

        return new FeedbackResultDTO(myFeedback, likeCount, dislikeCount);
    }

    public LikedUsersResponseDTO getLikedUsers(Long bsId) {
        BoughtSnack boughtSnack = boughtSnackRepository.findById(bsId)
                .orElseThrow(() -> new EntityNotFoundException("구매 과자를 찾을 수 없습니다."));

        List<BsFeedback> feedbacks = bsFeedbackRepository.findTop21ByBoughtSnackAndStatusOrderByIdDesc(
                boughtSnack, BsFeedback.FeedbackStatus.LIKE);

        boolean hasMore = feedbacks.size() > 20;
        List<UserNicknameDTO> users = feedbacks.stream()
                .limit(20)
                .map(f -> new UserNicknameDTO(f.getUser().getUserId(), f.getUser().getNickname()))
                .collect(Collectors.toList());

        return new LikedUsersResponseDTO(users, hasMore);
    }

    private BsFeedback.FeedbackStatus parseStatus(String status) {
        try {
            return BsFeedback.FeedbackStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new dev.ssafy.common.exception.BadRequestException("잘못된 피드백 상태값입니다. LIKE 또는 DISLIKE만 허용됩니다.");
        }
    }
}
