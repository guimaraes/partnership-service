package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.BonusPolicyRequest;
import br.com.ecomercial.partnership.dto.BonusPolicyResponse;
import br.com.ecomercial.partnership.entity.BonusPolicy;
import br.com.ecomercial.partnership.service.BonusPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bonus-policies")
@RequiredArgsConstructor
@Tag(name = "Bonus Policies", description = "Gestão de políticas de bônus por desempenho")
@SecurityRequirement(name = "Bearer Authentication")
public class BonusPolicyController {

    private final BonusPolicyService bonusPolicyService;

    @PostMapping
    @Operation(summary = "Criar política de bônus", description = "Cria uma nova política de bônus por desempenho")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Política de bônus criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Nome da política já existe")
    })
    public ResponseEntity<BonusPolicyResponse> createBonusPolicy(@Valid @RequestBody BonusPolicyRequest request) {
        BonusPolicyResponse response = bonusPolicyService.createBonusPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar políticas de bônus", description = "Retorna todas as políticas de bônus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de políticas obtida com sucesso")
    })
    public ResponseEntity<List<BonusPolicyResponse>> getAllBonusPolicies() {
        List<BonusPolicyResponse> policies = bonusPolicyService.getAllBonusPolicies();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter política por ID", description = "Retorna uma política de bônus específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Política encontrada"),
            @ApiResponse(responseCode = "404", description = "Política não encontrada")
    })
    public ResponseEntity<BonusPolicyResponse> getBonusPolicyById(
            @Parameter(description = "ID da política") @PathVariable Long id) {
        BonusPolicyResponse response = bonusPolicyService.getBonusPolicyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Obter política por nome", description = "Retorna uma política de bônus pelo nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Política encontrada"),
            @ApiResponse(responseCode = "404", description = "Política não encontrada")
    })
    public ResponseEntity<BonusPolicyResponse> getBonusPolicyByName(
            @Parameter(description = "Nome da política") @PathVariable String name) {
        BonusPolicyResponse response = bonusPolicyService.getBonusPolicyByName(name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar política de bônus", description = "Atualiza uma política de bônus existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Política atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Política não encontrada"),
            @ApiResponse(responseCode = "409", description = "Nome da política já existe")
    })
    public ResponseEntity<BonusPolicyResponse> updateBonusPolicy(
            @Parameter(description = "ID da política") @PathVariable Long id,
            @Valid @RequestBody BonusPolicyRequest request) {
        BonusPolicyResponse response = bonusPolicyService.updateBonusPolicy(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar política de bônus", description = "Desativa uma política de bônus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Política desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Política não encontrada")
    })
    public ResponseEntity<Void> deactivateBonusPolicy(
            @Parameter(description = "ID da política") @PathVariable Long id) {
        bonusPolicyService.deactivateBonusPolicy(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active/date")
    @Operation(summary = "Obter políticas ativas em uma data", description = "Retorna políticas ativas em uma data específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de políticas ativas obtida com sucesso")
    })
    public ResponseEntity<List<BonusPolicyResponse>> getActivePoliciesOnDate(
            @Parameter(description = "Data para consulta") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BonusPolicyResponse> policies = bonusPolicyService.getActiveBonusPoliciesOnDate(date);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/active/type/{type}/date")
    @Operation(summary = "Obter políticas ativas por tipo em uma data", description = "Retorna políticas ativas de um tipo específico em uma data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de políticas obtida com sucesso")
    })
    public ResponseEntity<List<BonusPolicyResponse>> getActivePoliciesByTypeOnDate(
            @Parameter(description = "Tipo de política") @PathVariable BonusPolicy.BonusType type,
            @Parameter(description = "Data para consulta") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BonusPolicyResponse> policies = bonusPolicyService.getActiveBonusPoliciesByTypeOnDate(type, date);
        return ResponseEntity.ok(policies);
    }
}

