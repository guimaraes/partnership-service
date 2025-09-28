package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Commission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionResponse {
    
    private Long id;
    private String partnerId;
    private Long clientId;
    private YearMonth referenceMonth;
    private BigDecimal clientBilling;
    private BigDecimal commissionValue;
    private BigDecimal bonusValue;
    private BigDecimal totalValue;
    private Commission.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

