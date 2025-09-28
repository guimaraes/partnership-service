package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Document is required")
    @Pattern(regexp = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", 
             message = "Document must be a valid CPF or CNPJ")
    private String document;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone must be valid")
    private String phone;
    
    @NotNull(message = "Client type is required")
    private Client.ClientType clientType;
    
    @Positive(message = "Monthly billing must be positive")
    private BigDecimal monthlyBilling;
}

