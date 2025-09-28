package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.CommissionRuleRequest;
import br.com.ecomercial.partnership.dto.CommissionRuleResponse;
import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.service.AuditService;
import br.com.ecomercial.partnership.service.CommissionRuleService;
import br.com.ecomercial.partnership.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/commission-rules")
@RequiredArgsConstructor
@Tag(name = "Commission Rules", description = "Gestão de regras de comissão versionadas")
@SecurityRequirement(name = "Bearer Authentication")
public class CommissionRuleController {
    
    private final CommissionRuleService commissionRuleService;
    private final ValidationService validationService;
    private final AuditService auditService;
    
    @PostMapping
    public ResponseEntity<CommissionRuleResponse> createCommissionRule(@Valid @RequestBody CommissionRuleRequest request, HttpServletRequest httpRequest) {
        // Validações de domínio
        validationService.validateCommissionRule(request);
        
        CommissionRuleResponse response = commissionRuleService.createCommissionRule(request);
        
        // Auditoria
        auditService.logCreate("COMMISSION_RULE", response.getId().toString(), response, httpRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CommissionRuleResponse>> getAllCommissionRules() {
        List<CommissionRuleResponse> rules = commissionRuleService.getAllCommissionRules();
        return ResponseEntity.ok(rules);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CommissionRuleResponse> getCommissionRuleById(@PathVariable Long id) {
        CommissionRuleResponse rule = commissionRuleService.getCommissionRuleById(id);
        return ResponseEntity.ok(rule);
    }
    
    @GetMapping("/client-type/{clientType}")
    public ResponseEntity<CommissionRuleResponse> getCommissionRuleByClientType(@PathVariable Client.ClientType clientType) {
        CommissionRuleResponse rule = commissionRuleService.getCommissionRuleByClientType(clientType);
        return ResponseEntity.ok(rule);
    }
    
    @GetMapping("/client-type/{clientType}/date")
    @Operation(summary = "Obter regra por tipo e data", description = "Retorna a regra ativa para um tipo de cliente em uma data específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regra encontrada"),
            @ApiResponse(responseCode = "404", description = "Regra não encontrada para a data")
    })
    public ResponseEntity<CommissionRuleResponse> getCommissionRuleByClientTypeAndDate(
            @Parameter(description = "Tipo de cliente") @PathVariable Client.ClientType clientType,
            @Parameter(description = "Data para consulta") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CommissionRuleResponse rule = commissionRuleService.getCommissionRuleByClientTypeAndDate(clientType, date);
        return ResponseEntity.ok(rule);
    }
    
    @GetMapping("/client-type/{clientType}/all")
    @Operation(summary = "Obter todas as regras por tipo", description = "Retorna todas as regras ativas para um tipo de cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de regras obtida com sucesso")
    })
    public ResponseEntity<List<CommissionRuleResponse>> getCommissionRulesForClientType(
            @Parameter(description = "Tipo de cliente") @PathVariable Client.ClientType clientType) {
        List<CommissionRuleResponse> rules = commissionRuleService.getCommissionRulesForClientType(clientType);
        return ResponseEntity.ok(rules);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CommissionRuleResponse> updateCommissionRule(
            @PathVariable Long id, 
            @Valid @RequestBody CommissionRuleRequest request) {
        CommissionRuleResponse response = commissionRuleService.updateCommissionRule(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCommissionRule(@PathVariable Long id) {
        commissionRuleService.deactivateCommissionRule(id);
        return ResponseEntity.noContent().build();
    }
}
