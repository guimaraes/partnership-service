package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    
    private Long id;
    private String name;
    private String document;
    private String email;
    private String phone;
    private Client.ClientType clientType;
    private BigDecimal monthlyBilling;
    private Client.Status status;
    private String partnerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

