package br.com.ecomercial.partnership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitação para reabertura de fechamento")
public class ReopenClosingRequest {

    @NotBlank(message = "Justification is required")
    @Size(max = 1000, message = "Justification must not exceed 1000 characters")
    @Schema(description = "Justificativa para reabertura", example = "Correção de dados de comissão", required = true)
    private String justification;
}

