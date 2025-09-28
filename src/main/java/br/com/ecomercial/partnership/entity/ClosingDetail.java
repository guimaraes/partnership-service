package br.com.ecomercial.partnership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "closing_details")
@EntityListeners(AuditingEntityListener.class)
public class ClosingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_closing_id", nullable = false)
    private MonthlyClosing monthlyClosing;

    @Column(name = "partner_id", nullable = false, length = 20)
    private String partnerId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "client_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Client.ClientType clientType;

    @Column(name = "client_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Client.Status clientStatus;

    @Column(name = "client_active_from")
    private LocalDate clientActiveFrom;

    @Column(name = "client_inactive_from")
    private LocalDate clientInactiveFrom;

    @Column(name = "monthly_revenue", precision = 15, scale = 2)
    private BigDecimal monthlyRevenue;

    @Column(name = "rule_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Client.ClientType ruleType;

    @Column(name = "commission_rate", precision = 10, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "commission_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal commissionValue;

    @Column(name = "bonus_value", precision = 15, scale = 2)
    private BigDecimal bonusValue;

    @Column(name = "total_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalValue;

    @Column(name = "observations", length = 500)
    private String observations;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isClientInactiveDuringMonth() {
        return clientInactiveFrom != null;
    }

    public String getStatusDescription() {
        if (isClientInactiveDuringMonth()) {
            return String.format("Cliente inativado em %s", clientInactiveFrom);
        }
        return "Cliente ativo durante todo o mês";
    }
}

