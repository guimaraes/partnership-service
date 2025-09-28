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
@Schema(description = "Resposta de autenticação")
public class AuthResponse {
    
    @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    
    @Schema(description = "Nome de usuário", example = "admin@app.io")
    private String username;
    
    @Schema(description = "Papel do usuário", example = "ADMIN", allowableValues = {"ADMIN", "PARTNER"})
    private String role;
    
    @Schema(description = "ID do parceiro (se aplicável)", example = "P-1001")
    private String partnerId;
}
