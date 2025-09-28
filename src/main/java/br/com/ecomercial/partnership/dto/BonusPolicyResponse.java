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
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta da política de bônus")
public class BonusPolicyResponse {

    @Schema(description = "ID da política", example = "1")
    private Long id;

    @Schema(description = "Nome da política", example = "BONUS_CLIENTES_ATIVOS")
    private String name;

    @Schema(description = "Descrição da política", example = "Bônus por número de clientes ativos")
    private String description;

    @Schema(description = "Tipo de política", example = "CLIENT_COUNT")
    private BonusPolicy.BonusType type;

    @Schema(description = "Número mínimo de clientes para bônus", example = "10")
    private Integer thresholdClients;

    @Schema(description = "Faturamento mínimo para bônus", example = "100000.00")
    private BigDecimal revenueThreshold;

    @Schema(description = "Valor do bônus", example = "300.00")
    private BigDecimal bonusAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de início da vigência", example = "2025-10-01")
    private LocalDate effectiveFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de fim da vigência", example = "2025-12-31")
    private LocalDate effectiveTo;

    @Schema(description = "Status da política", example = "ACTIVE")
    private BonusPolicy.Status status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação", example = "2025-01-27T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de última atualização", example = "2025-01-27T10:30:00")
    private LocalDateTime updatedAt;
}

