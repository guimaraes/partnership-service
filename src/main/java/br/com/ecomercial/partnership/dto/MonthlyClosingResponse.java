package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.MonthlyClosing;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta do fechamento mensal")
public class MonthlyClosingResponse {

    @Schema(description = "ID do fechamento", example = "1")
    private Long id;

    @JsonFormat(pattern = "yyyy-MM")
    @Schema(description = "Mês de referência", example = "2025-10")
    private String referenceMonth;

    @Schema(description = "Status do fechamento", example = "COMPLETED")
    private MonthlyClosing.Status status;

    @Schema(description = "Total de parceiros", example = "5")
    private Integer totalPartners;

    @Schema(description = "Total de clientes", example = "25")
    private Integer totalClients;

    @Schema(description = "Total de comissões", example = "2500.00")
    private BigDecimal totalCommission;

    @Schema(description = "Total de bônus", example = "800.00")
    private BigDecimal totalBonus;

    @Schema(description = "Total do payout", example = "3300.00")
    private BigDecimal totalPayout;

    @Schema(description = "Justificativa", example = "Reabertura para correção de dados")
    private String justification;

    @Schema(description = "Fechado por", example = "admin@app.io")
    private String closedBy;

    @Schema(description = "Reaberto por", example = "admin@app.io")
    private String reopenedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação", example = "2025-10-01T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de atualização", example = "2025-10-01T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Detalhes do fechamento")
    private List<ClosingDetailResponse> closingDetails;
}

