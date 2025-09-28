package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.ClientDashboardDetail;
import br.com.ecomercial.partnership.dto.PartnerDashboardResponse;
import br.com.ecomercial.partnership.service.PartnerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/partners/me")
@RequiredArgsConstructor
@Tag(name = "Partner Dashboard", description = "Dashboard do parceiro - visão de clientes ativos e comissões")
@SecurityRequirement(name = "Bearer Authentication")
public class PartnerDashboardController {

    private final PartnerDashboardService partnerDashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Obter dashboard do parceiro", description = "Retorna o sumário do mês corrente com clientes ativos, comissões estimadas e último fechamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard obtido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas para parceiros"),
            @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    })
    public ResponseEntity<PartnerDashboardResponse> getPartnerDashboard(
            @Parameter(description = "Mês de referência no formato YYYY-MM", example = "2025-10") 
            @RequestParam(required = false) String month) {
        
        YearMonth referenceMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        PartnerDashboardResponse dashboard = partnerDashboardService.getPartnerDashboard(referenceMonth);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/clients")
    @Operation(summary = "Obter detalhes dos clientes", description = "Retorna lista detalhada dos clientes do parceiro com comissões previstas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes dos clientes obtidos com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas para parceiros"),
            @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    })
    public ResponseEntity<List<ClientDashboardDetail>> getPartnerClientDetails(
            @Parameter(description = "Mês de referência no formato YYYY-MM", example = "2025-10") 
            @RequestParam(required = false) String month) {
        
        YearMonth referenceMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        List<ClientDashboardDetail> clientDetails = partnerDashboardService.getPartnerClientDetails(referenceMonth);
        return ResponseEntity.ok(clientDetails);
    }

    @GetMapping("/dashboard/summary")
    @Operation(summary = "Obter resumo rápido do dashboard", description = "Retorna apenas os números principais do dashboard (clientes ativos, comissão estimada, último fechamento)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo obtido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas para parceiros"),
            @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    })
    public ResponseEntity<PartnerDashboardResponse> getPartnerDashboardSummary(
            @Parameter(description = "Mês de referência no formato YYYY-MM", example = "2025-10") 
            @RequestParam(required = false) String month) {
        
        YearMonth referenceMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        PartnerDashboardResponse dashboard = partnerDashboardService.getPartnerDashboard(referenceMonth);
        
        // Retornar apenas o resumo (sem detalhes dos clientes)
        PartnerDashboardResponse summary = PartnerDashboardResponse.builder()
                .partnerId(dashboard.getPartnerId())
                .partnerName(dashboard.getPartnerName())
                .referenceMonth(dashboard.getReferenceMonth())
                .activeClients(dashboard.getActiveClients())
                .estimatedCommission(dashboard.getEstimatedCommission())
                .estimatedBonus(dashboard.getEstimatedBonus())
                .estimatedTotal(dashboard.getEstimatedTotal())
                .lastClosing(dashboard.getLastClosing())
                .partnerStatus(dashboard.getPartnerStatus())
                .totalClientBilling(dashboard.getTotalClientBilling())
                .averageClientBilling(dashboard.getAverageClientBilling())
                .clientDetails(null) // Sem detalhes dos clientes
                .build();
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/dashboard/performance")
    @Operation(summary = "Obter métricas de performance", description = "Retorna métricas de performance do parceiro para análise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas obtidas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas para parceiros"),
            @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    })
    public ResponseEntity<PartnerDashboardResponse> getPartnerPerformance(
            @Parameter(description = "Mês de referência no formato YYYY-MM", example = "2025-10") 
            @RequestParam(required = false) String month) {
        
        YearMonth referenceMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        PartnerDashboardResponse dashboard = partnerDashboardService.getPartnerDashboard(referenceMonth);
        return ResponseEntity.ok(dashboard);
    }
}