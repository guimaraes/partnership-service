package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Client;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitação para criação/atualização de regra de comissão")
public class CommissionRuleRequest {
    
    @NotNull(message = "Client type is required")
    @Schema(description = "Tipo de cliente", example = "TYPE_1", required = true)
    private Client.ClientType clientType;
    
    @NotNull(message = "Fixed commission is required")
    @Positive(message = "Fixed commission must be positive")
    @Schema(description = "Comissão fixa", example = "100.00", required = true)
    private BigDecimal fixedCommission;
    
    @DecimalMin(value = "0.0", message = "Percentage commission must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Percentage commission must be between 0 and 1")
    @Schema(description = "Comissão percentual", example = "0.05")
    private BigDecimal percentageCommission;
    
    @Positive(message = "Min billing threshold must be positive")
    @Schema(description = "Limite mínimo de faturamento", example = "1000.00")
    private BigDecimal minBillingThreshold;
    
    @Positive(message = "Max billing threshold must be positive")
    @Schema(description = "Limite máximo de faturamento", example = "5000.00")
    private BigDecimal maxBillingThreshold;
    
    @NotNull(message = "Effective from date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de início da vigência", example = "2025-10-01", required = true)
    private LocalDate effectiveFrom;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de fim da vigência", example = "2025-12-31")
    private LocalDate effectiveTo;
}
