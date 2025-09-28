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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta do relatório detalhado por parceiro")
public class PartnerDetailReportResponse {

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

    @JsonFormat(pattern = "yyyy-MM")
    @Schema(description = "Mês de referência", example = "2025-10")
    private String referenceMonth;

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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação do relatório", example = "2025-10-01T10:30:00")
    private LocalDateTime reportDate;

    @Schema(description = "Detalhes dos clientes")
    private List<ClientReportDetail> clientDetails;

    @Schema(description = "Políticas de bônus aplicadas")
    private List<BonusPolicyApplied> bonusPoliciesApplied;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalhe do cliente no relatório")
    public static class ClientReportDetail {

        @Schema(description = "ID do cliente", example = "1")
        private Long clientId;

        @Schema(description = "Nome do cliente", example = "Acme SA")
        private String clientName;

        @Schema(description = "Tipo do cliente", example = "TYPE_1")
        private Client.ClientType clientType;

        @Schema(description = "Status do cliente", example = "ACTIVE")
        private Client.Status clientStatus;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "Data de ativação", example = "2025-09-10")
        private LocalDate activeFrom;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "Data de inativação", example = "2025-10-15")
        private LocalDate inactiveFrom;

        @Schema(description = "Faturamento mensal", example = "10000.00")
        private BigDecimal monthlyBilling;

        @Schema(description = "Comissão calculada", example = "200.00")
        private BigDecimal commissionValue;

        @Schema(description = "Bônus calculado", example = "50.00")
        private BigDecimal bonusValue;

        @Schema(description = "Total calculado", example = "250.00")
        private BigDecimal totalValue;

        @Schema(description = "Observações", example = "Cliente inativado em 2025-10-15")
        private String observations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Política de bônus aplicada")
    public static class BonusPolicyApplied {

        @Schema(description = "Nome da política", example = "BONUS_CLIENTES_ATIVOS")
        private String policyName;

        @Schema(description = "Tipo da política", example = "CLIENT_COUNT")
        private String policyType;

        @Schema(description = "Critério atingido", example = "12 clientes ativos")
        private String criteriaMet;

        @Schema(description = "Valor do bônus", example = "300.00")
        private BigDecimal bonusAmount;

        @Schema(description = "Descrição da política", example = "Bônus por ter 10+ clientes ativos")
        private String description;
    }
}