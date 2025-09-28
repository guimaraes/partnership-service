package br.com.ecomercial.partnership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "commissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "partner_id", nullable = false)
    private String partnerId;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "reference_month", nullable = false)
    @Convert(converter = YearMonthConverter.class)
    private YearMonth referenceMonth;
    
    @Column(name = "client_billing", precision = 15, scale = 2, nullable = false)
    private BigDecimal clientBilling;
    
    @Column(name = "commission_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal commissionValue;
    
    @Column(name = "bonus_value", precision = 15, scale = 2)
    private BigDecimal bonusValue;
    
    @Column(name = "total_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalValue;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", insertable = false, updatable = false)
    private Partner partner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;
    
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
        PENDING, APPROVED, PAID, CANCELLED
    }
}
