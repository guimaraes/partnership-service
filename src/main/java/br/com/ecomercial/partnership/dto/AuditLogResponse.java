package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.AuditLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de log de auditoria")
public class AuditLogResponse {

    @Schema(description = "ID do log de auditoria", example = "1")
    private Long id;

    @Schema(description = "Tipo da entidade", example = "COMMISSION_RULE")
    private String entityType;

    @Schema(description = "ID da entidade", example = "CR-1001")
    private String entityId;

    @Schema(description = "Ação realizada", example = "CREATE")
    private String action;

    @Schema(description = "ID do usuário", example = "1")
    private String userId;

    @Schema(description = "Nome do usuário", example = "admin")
    private String userName;

    @Schema(description = "Valores antigos (JSON)")
    private String oldValues;

    @Schema(description = "Valores novos (JSON)")
    private String newValues;

    @Schema(description = "Justificativa", example = "Ajuste de valores")
    private String justification;

    @Schema(description = "Endereço IP", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "User Agent", example = "Mozilla/5.0...")
    private String userAgent;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data e hora da ação", example = "2025-10-01T10:00:00")
    private LocalDateTime createdAt;
}
