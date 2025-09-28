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
@Schema(description = "Solicitação para alteração de tipo de cliente")
public class ClientTypeChangeRequest {
    
    @NotNull(message = "Client type is required")
    @Schema(description = "Novo tipo de cliente", example = "TYPE_2", required = true)
    private Client.ClientType clientType;
    
    @NotNull(message = "Effective from date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de vigência da alteração", example = "2025-10-01", required = true)
    private LocalDate effectiveFrom;
}

