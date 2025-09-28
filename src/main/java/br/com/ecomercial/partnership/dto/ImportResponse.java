package br.com.ecomercial.partnership.dto;

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
@Schema(description = "Resposta de importação")
public class ImportResponse {

    @Schema(description = "ID do job de importação", example = "import-12345")
    private String jobId;

    @Schema(description = "Tipo de importação", example = "PARTNERS")
    private String importType;

    @Schema(description = "Status do job", example = "ACCEPTED", allowableValues = {"ACCEPTED", "PROCESSING", "COMPLETED", "FAILED"})
    private String status;

    @Schema(description = "Nome do arquivo", example = "partners.csv")
    private String fileName;

    @Schema(description = "Número total de linhas processadas", example = "100")
    private Integer totalLines;

    @Schema(description = "Número de linhas processadas com sucesso", example = "95")
    private Integer successLines;

    @Schema(description = "Número de linhas com erro", example = "5")
    private Integer errorLines;

    @Schema(description = "Mensagem de status", example = "Importação iniciada com sucesso")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data e hora de início da importação", example = "2025-10-01T10:00:00")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data e hora de conclusão da importação", example = "2025-10-01T10:05:00")
    private LocalDateTime completedAt;
}
