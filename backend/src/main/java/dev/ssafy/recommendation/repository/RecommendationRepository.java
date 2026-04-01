package dev.ssafy.recommendation.repository;

import dev.ssafy.recommendation.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * plan.md Phase 1 기준
 */
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    /**
     * 전체 목록 조회 (Pageable로 정렬/페이지네이션 처리)
     * 정렬 기준: 미정 사항 (plan.md 7번) — 호출 측에서 Pageable로 전달
     */
    Page<Recommendation> findAll(Pageable pageable);
}
