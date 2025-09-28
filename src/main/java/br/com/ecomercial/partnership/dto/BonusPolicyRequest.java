package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.BonusPolicy;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
@Schema(description = "Solicitação para criação/atualização de política de bônus")
public class BonusPolicyRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Nome da política de bônus", example = "BONUS_CLIENTES_ATIVOS", required = true)
    private String name;

    @Schema(description = "Descrição da política", example = "Bônus por número de clientes ativos")
    private String description;

    @NotNull(message = "Type is required")
    @Schema(description = "Tipo de política de bônus", example = "CLIENT_COUNT", required = true)
    private BonusPolicy.BonusType type;

    @Schema(description = "Número mínimo de clientes para bônus (apenas para tipo CLIENT_COUNT)", example = "10")
    private Integer thresholdClients;

    @Schema(description = "Faturamento mínimo para bônus (apenas para tipo REVENUE_THRESHOLD)", example = "100000.00")
    private BigDecimal revenueThreshold;

    @NotNull(message = "Bonus amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bonus amount must be greater than 0")
    @Schema(description = "Valor do bônus", example = "300.00", required = true)
    private BigDecimal bonusAmount;

    @NotNull(message = "Effective from date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de início da vigência", example = "2025-10-01", required = true)
    private LocalDate effectiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de fim da vigência", example = "2025-12-31")
    private LocalDate effectiveTo;
}

