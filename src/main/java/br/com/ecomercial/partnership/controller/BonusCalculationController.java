package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.BonusCalculationResponse;
import br.com.ecomercial.partnership.repository.PartnerRepository;
import br.com.ecomercial.partnership.service.BonusCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/bonus-calculations")
@RequiredArgsConstructor
@Tag(name = "Bonus Calculations", description = "Cálculos de bônus por desempenho")
@SecurityRequirement(name = "Bearer Authentication")
public class BonusCalculationController {

    private final BonusCalculationService bonusCalculationService;
    private final PartnerRepository partnerRepository;

    @GetMapping("/partner/{partnerId}/period/{period}")
    @Operation(summary = "Calcular bônus por parceiro e período", description = "Calcula bônus de desempenho para um parceiro em um período específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculo de bônus realizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<BonusCalculationResponse> calculateBonusForPartner(
            @Parameter(description = "ID do parceiro") @PathVariable String partnerId,
            @Parameter(description = "Período no formato YYYY-MM") @PathVariable String period) {
        
        // Verificar autorização
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        br.com.ecomercial.partnership.entity.User currentUser = (br.com.ecomercial.partnership.entity.User) auth.getPrincipal();
        
        if (currentUser.getRole() == br.com.ecomercial.partnership.entity.User.Role.PARTNER && 
            !partnerId.equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        YearMonth referenceMonth = YearMonth.parse(period);
        BonusCalculationResponse response = bonusCalculationService.calculateBonusForPartner(partnerId, referenceMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/partner/{partnerId}/current-month")
    @Operation(summary = "Calcular bônus para o mês atual", description = "Calcula bônus de desempenho para um parceiro no mês atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculo de bônus realizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<BonusCalculationResponse> calculateBonusForCurrentMonth(
            @Parameter(description = "ID do parceiro") @PathVariable String partnerId) {
        
        // Verificar autorização
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        br.com.ecomercial.partnership.entity.User currentUser = (br.com.ecomercial.partnership.entity.User) auth.getPrincipal();
        
        if (currentUser.getRole() == br.com.ecomercial.partnership.entity.User.Role.PARTNER && 
            !partnerId.equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        YearMonth currentMonth = YearMonth.now();
        BonusCalculationResponse response = bonusCalculationService.calculateBonusForPartner(partnerId, currentMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-partners/period/{period}")
    @Operation(summary = "Calcular bônus para todos os parceiros", description = "Calcula bônus de desempenho para todos os parceiros ativos em um período")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculos de bônus realizados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Apenas administradores podem acessar")
    })
    public ResponseEntity<List<BonusCalculationResponse>> calculateBonusForAllPartners(
            @Parameter(description = "Período no formato YYYY-MM") @PathVariable String period) {
        
        // Verificar se é admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        br.com.ecomercial.partnership.entity.User currentUser = (br.com.ecomercial.partnership.entity.User) auth.getPrincipal();
        
        if (currentUser.getRole() != br.com.ecomercial.partnership.entity.User.Role.ADMIN) {
            throw new RuntimeException("Access denied - Admin role required");
        }
        
        YearMonth referenceMonth = YearMonth.parse(period);
        List<BonusCalculationResponse> responses = bonusCalculationService.calculateBonusForAllPartners(referenceMonth);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-bonus/current-month")
    @Operation(summary = "Meu bônus do mês atual", description = "Calcula bônus de desempenho do usuário logado para o mês atual. Para admins, retorna bônus do primeiro parceiro encontrado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculo de bônus realizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<BonusCalculationResponse> calculateMyBonusForCurrentMonth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        br.com.ecomercial.partnership.entity.User currentUser = (br.com.ecomercial.partnership.entity.User) auth.getPrincipal();
        
        String partnerId;
        YearMonth currentMonth = YearMonth.now();
        
        if (currentUser.getRole() == br.com.ecomercial.partnership.entity.User.Role.PARTNER) {
            // Parceiro: usar seu próprio partnerId
            partnerId = currentUser.getPartnerId();
        } else if (currentUser.getRole() == br.com.ecomercial.partnership.entity.User.Role.ADMIN) {
            // Admin: buscar o primeiro parceiro ativo
            partnerId = getFirstActivePartnerId();
            if (partnerId == null) {
                throw new RuntimeException("No active partners found");
            }
        } else {
            throw new RuntimeException("Access denied - invalid user role");
        }
        
        BonusCalculationResponse response = bonusCalculationService.calculateBonusForPartner(partnerId, currentMonth);
        return ResponseEntity.ok(response);
    }
    
    private String getFirstActivePartnerId() {
        // Buscar o primeiro parceiro ativo
        return partnerRepository.findByStatus(br.com.ecomercial.partnership.entity.Partner.Status.ACTIVE)
                .stream()
                .findFirst()
                .map(br.com.ecomercial.partnership.entity.Partner::getPartnerId)
                .orElse(null);
    }
}

