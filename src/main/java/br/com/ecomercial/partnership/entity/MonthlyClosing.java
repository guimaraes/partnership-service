package br.com.ecomercial.partnership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "monthly_closings")
@EntityListeners(AuditingEntityListener.class)
public class MonthlyClosing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_month", nullable = false, unique = true, columnDefinition = "VARCHAR(7)")
    private YearMonth referenceMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "total_partners")
    private Integer totalPartners;

    @Column(name = "total_clients")
    private Integer totalClients;

    @Column(name = "total_commission", precision = 15, scale = 2)
    private BigDecimal totalCommission;

    @Column(name = "total_bonus", precision = 15, scale = 2)
    private BigDecimal totalBonus;

    @Column(name = "total_payout", precision = 15, scale = 2)
    private BigDecimal totalPayout;

    @Column(name = "justification", length = 1000)
    private String justification;

    @Column(name = "closed_by")
    private String closedBy;

    @Column(name = "reopened_by")
    private String reopenedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "monthlyClosing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClosingDetail> closingDetails;

    public enum Status {
        IN_PROGRESS,    // Em processamento
        COMPLETED,      // Fechamento concluído
        REOPENED,       // Reaberto para correções
        CANCELLED       // Cancelado
    }

    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    public boolean isReopened() {
        return status == Status.REOPENED;
    }

    public boolean canBeReopened() {
        return status == Status.COMPLETED;
    }

    public boolean canBeExecuted() {
        return status != Status.COMPLETED;
    }
}
