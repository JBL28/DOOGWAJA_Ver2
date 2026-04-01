package dev.ssafy.rc_comment.repository;

import dev.ssafy.rc_comment.entity.RcComment;
import dev.ssafy.recommendation.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * plan.md Phase 1 기준
 */
public interface RcCommentRepository extends JpaRepository<RcComment, Long> {

    /**
     * 추천글별 댓글 페이지네이션 조회 (RC-1: createdAt ASC)
     */
    Page<RcComment> findByRecommendation(Recommendation recommendation, Pageable pageable);

    /**
     * 추천글별 전체 댓글 조회 (commentPreview 최대 3개용)
     */
    List<RcComment> findByRecommendation(Recommendation recommendation, org.springframework.data.domain.Sort sort);

    /**
     * 추천글별 댓글 수 조회 (commentCount용)
     */
    long countByRecommendation(Recommendation recommendation);
}
