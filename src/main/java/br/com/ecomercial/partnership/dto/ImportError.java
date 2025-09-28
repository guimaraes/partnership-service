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
@Schema(description = "Erro de importação")
public class ImportError {

    @Schema(description = "Número da linha com erro", example = "5")
    private Integer lineNumber;

    @Schema(description = "Conteúdo da linha com erro", example = "P-1001,Contabil Alfa,12345678901")
    private String lineContent;

    @Schema(description = "Campo com erro", example = "document")
    private String field;

    @Schema(description = "Mensagem de erro", example = "Documento já existe")
    private String errorMessage;

    @Schema(description = "Valor inválido", example = "12345678901")
    private String invalidValue;
}
