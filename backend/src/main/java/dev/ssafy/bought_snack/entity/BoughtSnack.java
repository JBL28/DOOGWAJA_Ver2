package dev.ssafy.bought_snack.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * plan.md 1 기준
 * ERD: bought_snack 테이블
 */
@Entity
@Table(name = "bought_snack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoughtSnack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bs_id")
    private Long bsId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SnackStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = SnackStatus.SHIPPING; // Default
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public BoughtSnack(String name, SnackStatus status) {
        this.name = name;
        this.status = status != null ? status : SnackStatus.SHIPPING;
    }

    public void update(String name) {
        this.name = name;
    }

    public void updateStatus(SnackStatus status) {
        this.status = status;
    }

    public enum SnackStatus {
        SHIPPING("배송중"),
        IN_STOCK("재고있음"),
        OUT_OF_STOCK("재고없음");

        private final String label;

        SnackStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
