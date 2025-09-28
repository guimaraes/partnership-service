package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.entity.CommissionRule;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta da regra de comissão")
public class CommissionRuleResponse {
    
    @Schema(description = "ID da regra", example = "1")
    private Long id;
    
    @Schema(description = "Tipo de cliente", example = "TYPE_1")
    private Client.ClientType clientType;
    
    @Schema(description = "Comissão fixa", example = "100.00")
    private BigDecimal fixedCommission;
    
    @Schema(description = "Comissão percentual", example = "0.05")
    private BigDecimal percentageCommission;
    
    @Schema(description = "Limite mínimo de faturamento", example = "1000.00")
    private BigDecimal minBillingThreshold;
    
    @Schema(description = "Limite máximo de faturamento", example = "5000.00")
    private BigDecimal maxBillingThreshold;
    
    @Schema(description = "Status da regra", example = "ACTIVE")
    private CommissionRule.Status status;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de início da vigência", example = "2025-10-01")
    private LocalDate effectiveFrom;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de fim da vigência", example = "2025-12-31")
    private LocalDate effectiveTo;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação", example = "2025-09-26T10:30:00")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de atualização", example = "2025-09-26T10:30:00")
    private LocalDateTime updatedAt;
}
