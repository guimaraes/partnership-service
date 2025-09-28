package br.com.ecomercial.partnership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de autenticação")
public class AuthRequest {
    
    @NotBlank(message = "Username is required")
    @Schema(description = "Nome de usuário", example = "admin@app.io", required = true)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Schema(description = "Senha do usuário", example = "admin123", required = true)
    private String password;
}
