package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Client;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitação para alteração de status de cliente")
public class ClientStatusChangeRequest {
    
    @NotNull(message = "Status is required")
    @Schema(description = "Novo status do cliente", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, required = true)
    private Client.Status status;
    
    @NotNull(message = "Effective from date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de vigência da alteração", example = "2025-10-05", required = true)
    private LocalDate effectiveFrom;
}

