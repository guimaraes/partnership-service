package br.com.ecomercial.partnership.dto;

import br.com.ecomercial.partnership.entity.Partner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponse {
    
    private String partnerId;
    private String name;
    private String document;
    private String email;
    private String phone;
    private Partner.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

