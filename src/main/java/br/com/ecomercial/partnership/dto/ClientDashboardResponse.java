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
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta do cliente no dashboard")
public class ClientDashboardResponse {

    @Schema(description = "ID do cliente", example = "1")
    private Long clientId;

    @Schema(description = "Nome do cliente", example = "Acme SA")
    private String clientName;

    @Schema(description = "Tipo do cliente", example = "TYPE_1")
    private Client.ClientType clientType;

    @Schema(description = "Status do cliente", example = "ACTIVE")
    private Client.Status clientStatus;

    @Schema(description = "Faturamento mensal", example = "10000.00")
    private BigDecimal monthlyBilling;

    @Schema(description = "Comissão prevista para o mês", example = "200.00")
    private BigDecimal estimatedCommission;

    @Schema(description = "Bônus previsto para o mês", example = "50.00")
    private BigDecimal estimatedBonus;

    @Schema(description = "Total previsto para o mês", example = "250.00")
    private BigDecimal estimatedTotal;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de ativação do cliente", example = "2025-09-10")
    private LocalDate clientActiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de inativação do cliente (se aplicável)", example = "2025-10-15")
    private LocalDate clientInactiveFrom;

    @Schema(description = "Observações sobre o status do cliente", example = "Cliente inativado em 2025-10-15")
    private String statusDescription;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação do cliente", example = "2025-09-10T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Indica se o cliente está ativo durante todo o mês", example = "true")
    private Boolean activeAllMonth;

    @Schema(description = "Dias ativos no mês (se inativado durante o mês)", example = "15")
    private Integer activeDaysInMonth;
}