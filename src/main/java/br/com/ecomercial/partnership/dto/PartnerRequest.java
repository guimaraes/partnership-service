package br.com.ecomercial.partnership.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRequest {
    
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
}

