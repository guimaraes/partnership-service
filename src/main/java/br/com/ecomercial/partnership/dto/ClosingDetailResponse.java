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
@Schema(description = "Resposta do detalhe do fechamento")
public class ClosingDetailResponse {

    @Schema(description = "ID do detalhe", example = "1")
    private Long id;

    @Schema(description = "ID do parceiro", example = "P-1001")
    private String partnerId;

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
    private LocalDate clientActiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de inativação do cliente", example = "2025-10-15")
    private LocalDate clientInactiveFrom;

    @Schema(description = "Faturamento mensal", example = "10000.00")
    private BigDecimal monthlyRevenue;

    @Schema(description = "Tipo da regra aplicada", example = "TYPE_1")
    private Client.ClientType ruleType;

    @Schema(description = "Taxa de comissão", example = "0.0100")
    private BigDecimal commissionRate;

    @Schema(description = "Valor da comissão", example = "100.00")
    private BigDecimal commissionValue;

    @Schema(description = "Valor do bônus", example = "50.00")
    private BigDecimal bonusValue;

    @Schema(description = "Valor total", example = "150.00")
    private BigDecimal totalValue;

    @Schema(description = "Observações", example = "Cliente inativado em 2025-10-15")
    private String observations;

    @Schema(description = "Descrição do status", example = "Cliente inativado em 2025-10-15")
    private String statusDescription;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação", example = "2025-10-01T10:30:00")
    private LocalDateTime createdAt;
}

