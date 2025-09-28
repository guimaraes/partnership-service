package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.BonusPolicy;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta do cálculo de bônus para um parceiro")
public class BonusCalculationResponse {

    @Schema(description = "ID do parceiro", example = "P-1001")
    private String partnerId;

    @JsonFormat(pattern = "yyyy-MM")
    @Schema(description = "Período de cálculo", example = "2025-10")
    private String period;

    @Schema(description = "Número de clientes ativos no período", example = "12")
    private Integer activeClientsCount;

    @Schema(description = "Faturamento total dos clientes ativos", example = "120000.00")
    private BigDecimal totalRevenue;

    @Schema(description = "Total de bônus aplicados", example = "800.00")
    private BigDecimal totalBonus;

    @Schema(description = "Detalhamento dos bônus aplicados")
    private List<BonusDetail> bonusDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalhe de um bônus aplicado")
    public static class BonusDetail {

        @Schema(description = "Nome da política de bônus", example = "BONUS_CLIENTES_ATIVOS")
        private String policyName;

        @Schema(description = "Tipo de política", example = "CLIENT_COUNT")
        private BonusPolicy.BonusType type;

        @Schema(description = "Valor do bônus aplicado", example = "300.00")
        private BigDecimal bonusAmount;

        @Schema(description = "Descrição do critério atendido", example = "12 clientes ativos (threshold: 10)")
        private String description;
    }
}

