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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bonus_policies")
@EntityListeners(AuditingEntityListener.class)
public class BonusPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BonusType type;

    @Column(name = "threshold_clients")
    private Integer thresholdClients;

    @Column(name = "revenue_threshold", precision = 10, scale = 2)
    private BigDecimal revenueThreshold;

    @Column(name = "bonus_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal bonusAmount;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BonusType {
        CLIENT_COUNT,      // Bônus por número de clientes ativos
        REVENUE_THRESHOLD  // Bônus por faturamento agregado
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean isEffectiveOn(LocalDate date) {
        return isActive() && 
               !effectiveFrom.isAfter(date) && 
               (effectiveTo == null || !effectiveTo.isBefore(date));
    }
}

