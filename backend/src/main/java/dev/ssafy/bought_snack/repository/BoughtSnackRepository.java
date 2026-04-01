package dev.ssafy.bought_snack.repository;

import dev.ssafy.bought_snack.entity.BoughtSnack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoughtSnackRepository extends JpaRepository<BoughtSnack, Long> {

    /**
     * 구매 과자 목록 페이지네이션 조회 (R-1: createdAt DESC 등)
     */
    Page<BoughtSnack> findAll(Pageable pageable);
}
