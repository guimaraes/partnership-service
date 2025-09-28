package br.com.ecomercial.partnership.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de erro padronizada")
public class ErrorResponse {

    @Schema(description = "Código de erro", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Mensagem de erro", example = "Validation failed")
    private String message;

    @Schema(description = "Detalhes dos erros por campo")
    private Map<String, String> fieldErrors;

    @Schema(description = "Caminho da requisição", example = "/api/partners")
    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp do erro", example = "2025-10-01T10:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "Status HTTP", example = "400")
    private int status;
}
