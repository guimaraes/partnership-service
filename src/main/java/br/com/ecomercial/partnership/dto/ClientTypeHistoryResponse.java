package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Client;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Histórico de tipos de cliente")
public class ClientTypeHistoryResponse {
    
    @Schema(description = "ID do histórico", example = "1")
    private Long id;
    
    @Schema(description = "ID do cliente", example = "2001")
    private Long clientId;
    
    @Schema(description = "Tipo de cliente", example = "TYPE_2")
    private Client.ClientType clientType;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de início da vigência", example = "2025-10-01")
    private LocalDate effectiveFrom;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de fim da vigência", example = "2025-12-31")
    private LocalDate effectiveTo;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação", example = "2025-09-26T10:30:00")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de atualização", example = "2025-09-26T10:30:00")
    private LocalDateTime updatedAt;
}

