package br.com.ecomercial.partnership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false)
    private Client.ClientType clientType;
    
    @Column(name = "fixed_commission", precision = 15, scale = 2, nullable = false)
    private BigDecimal fixedCommission;
    
    @Column(name = "percentage_commission", precision = 5, scale = 4)
    private BigDecimal percentageCommission;
    
    @Column(name = "min_billing_threshold", precision = 15, scale = 2)
    private BigDecimal minBillingThreshold;
    
    @Column(name = "max_billing_threshold", precision = 15, scale = 2)
    private BigDecimal maxBillingThreshold;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum Status {
        ACTIVE, INACTIVE
    }
}
