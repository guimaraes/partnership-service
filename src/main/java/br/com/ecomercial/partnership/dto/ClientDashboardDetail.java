package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Client;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Detalhe do cliente no dashboard")
public class ClientDashboardDetail {

    @Schema(description = "ID do cliente", example = "1")
    private Long clientId;

    @Schema(description = "Nome do cliente", example = "Acme SA")
    private String clientName;

    @Schema(description = "Tipo do cliente", example = "TYPE_1")
    private Client.ClientType clientType;

    @Schema(description = "Status do cliente", example = "ACTIVE")
    private Client.Status clientStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de ativação do cliente", example = "2025-09-10")
    private LocalDate activeFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de inativação do cliente (se aplicável)", example = "2025-10-15")
    private LocalDate inactiveFrom;

    @Schema(description = "Faturamento mensal do cliente", example = "10000.00")
    private BigDecimal monthlyBilling;

    @Schema(description = "Comissão prevista para o cliente", example = "200.00")
    private BigDecimal estimatedCommission;

    @Schema(description = "Bônus previsto para o cliente", example = "50.00")
    private BigDecimal estimatedBonus;

    @Schema(description = "Total previsto para o cliente", example = "250.00")
    private BigDecimal estimatedTotal;

    @Schema(description = "Observações sobre o cliente", example = "Cliente inativado em 2025-10-15")
    private String observations;

    @Schema(description = "Descrição do status", example = "Cliente ativo durante todo o mês")
    private String statusDescription;
}
