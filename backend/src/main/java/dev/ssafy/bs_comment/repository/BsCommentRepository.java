package dev.ssafy.bs_comment.repository;

import dev.ssafy.bought_snack.entity.BoughtSnack;
import dev.ssafy.bs_comment.entity.BsComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BsCommentRepository extends JpaRepository<BsComment, Long> {

    /**
     * 과자별 댓글 페이지네이션 조회 (createdAt ASC)
     */
    Page<BsComment> findByBoughtSnack(BoughtSnack boughtSnack, Pageable pageable);

    /**
     * 과자별 전체 댓글 조회 (commentPreview 최대 3개용)
     */
    List<BsComment> findByBoughtSnack(BoughtSnack boughtSnack, Sort sort);

    /**
     * 과자별 댓글 수 조회 (commentCount용)
     */
    long countByBoughtSnack(BoughtSnack boughtSnack);
}
