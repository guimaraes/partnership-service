package br.com.ecomercial.partnership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta do relatório por parceiro")
public class PartnerReportResponse {

    @Schema(description = "ID do parceiro", example = "P-1001")
    private String partnerId;

    @Schema(description = "Nome do parceiro", example = "Contabil Alfa")
    private String partnerName;

    @Schema(description = "Email do parceiro", example = "alfa@conta.br")
    private String partnerEmail;

    @Schema(description = "Telefone do parceiro", example = "11999999999")
    private String partnerPhone;

    @Schema(description = "Status do parceiro", example = "ACTIVE")
    private String partnerStatus;

    @Schema(description = "Total de comissões", example = "1200.00")
    private BigDecimal totalCommission;

    @Schema(description = "Total de bônus", example = "300.00")
    private BigDecimal totalBonus;

    @Schema(description = "Total geral", example = "1500.00")
    private BigDecimal grandTotal;

    @Schema(description = "Número de clientes ativos", example = "12")
    private Integer activeClients;

    @Schema(description = "Faturamento total dos clientes", example = "50000.00")
    private BigDecimal totalClientBilling;

    @Schema(description = "Data de criação do relatório", example = "2025-10-01T10:30:00")
    private LocalDateTime reportDate;
}