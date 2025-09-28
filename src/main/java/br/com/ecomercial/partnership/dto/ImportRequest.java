package br.com.ecomercial.partnership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de importação")
public class ImportRequest {

    @Schema(description = "Tipo de importação", example = "PARTNERS", allowableValues = {"PARTNERS", "CLIENTS"})
    private String importType;

    @Schema(description = "Nome do arquivo", example = "partners.csv")
    private String fileName;

    @Schema(description = "Tamanho do arquivo em bytes", example = "1024")
    private Long fileSize;

    @Schema(description = "Número total de linhas no arquivo", example = "100")
    private Integer totalLines;
}
