package br.com.ecomercial.partnership.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta do dashboard do parceiro")
public class PartnerDashboardResponse {

    @Schema(description = "ID do parceiro", example = "P-1001")
    private String partnerId;

    @Schema(description = "Nome do parceiro", example = "Contabil Alfa")
    private String partnerName;

    @JsonFormat(pattern = "yyyy-MM")
    @Schema(description = "Mês de referência", example = "2025-10")
    private String referenceMonth;

    @Schema(description = "Número de clientes ativos", example = "12")
    private Integer activeClients;

    @Schema(description = "Comissão estimada para o mês", example = "1200.00")
    private BigDecimal estimatedCommission;

    @Schema(description = "Bônus estimado para o mês", example = "300.00")
    private BigDecimal estimatedBonus;

    @Schema(description = "Total estimado para o mês", example = "1500.00")
    private BigDecimal estimatedTotal;

    @JsonFormat(pattern = "yyyy-MM")
    @Schema(description = "Último fechamento realizado", example = "2025-09")
    private String lastClosing;

    @Schema(description = "Status do parceiro", example = "ACTIVE")
    private String partnerStatus;

    @Schema(description = "Faturamento total dos clientes ativos", example = "50000.00")
    private BigDecimal totalClientBilling;

    @Schema(description = "Média de faturamento por cliente", example = "4166.67")
    private BigDecimal averageClientBilling;

    @Schema(description = "Detalhes dos clientes")
    private java.util.List<ClientDashboardDetail> clientDetails;
}