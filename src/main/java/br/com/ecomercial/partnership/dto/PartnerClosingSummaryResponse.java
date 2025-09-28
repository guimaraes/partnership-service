package br.com.ecomercial.partnership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumo do fechamento por parceiro")
public class PartnerClosingSummaryResponse {

    @Schema(description = "ID do parceiro", example = "P-1001")
    private String partnerId;

    @Schema(description = "Nome do parceiro", example = "Contabil Alfa")
    private String partnerName;

    @Schema(description = "Total de clientes", example = "5")
    private Integer totalClients;

    @Schema(description = "Total de comissões", example = "500.00")
    private BigDecimal totalCommission;

    @Schema(description = "Total de bônus", example = "200.00")
    private BigDecimal totalBonus;

    @Schema(description = "Total do payout", example = "700.00")
    private BigDecimal totalPayout;

    @Schema(description = "Detalhes por cliente")
    private List<ClosingDetailResponse> clientDetails;
}

