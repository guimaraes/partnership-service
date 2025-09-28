package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.*;
import br.com.ecomercial.partnership.service.AuditService;
import br.com.ecomercial.partnership.service.MonthlyClosingService;
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

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/closings")
@RequiredArgsConstructor
@Tag(name = "Monthly Closings", description = "Gestão de fechamentos mensais de comissões")
@SecurityRequirement(name = "Bearer Authentication")
public class MonthlyClosingController {

    private final MonthlyClosingService monthlyClosingService;
    private final AuditService auditService;

    @PostMapping("/run")
    @Operation(summary = "Executar fechamento mensal", description = "Executa o fechamento mensal para um mês específico, calculando comissões e bônus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Fechamento iniciado com sucesso"),
            @ApiResponse(responseCode = "409", description = "Fechamento já existe para o mês"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<MonthlyClosingResponse> runMonthlyClosing(
            @Parameter(description = "Mês de referência no formato YYYY-MM", example = "2025-10") 
            @RequestParam String month) {
        
        YearMonth referenceMonth = YearMonth.parse(month);
        MonthlyClosingResponse response = monthlyClosingService.runMonthlyClosing(referenceMonth);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/run-with-request")
    @Operation(summary = "Executar fechamento mensal com request body", description = "Executa o fechamento mensal usando request body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Fechamento iniciado com sucesso"),
            @ApiResponse(responseCode = "409", description = "Fechamento já existe para o mês"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<MonthlyClosingResponse> runMonthlyClosingWithRequest(
            @Valid @RequestBody ClosingRequest request) {
        
        YearMonth referenceMonth = YearMonth.parse(request.getMonth());
        MonthlyClosingResponse response = monthlyClosingService.runMonthlyClosing(referenceMonth);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{year}/{month}/reopen")
    @Operation(summary = "Reabrir fechamento mensal", description = "Reabre um fechamento mensal para correções administrativas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fechamento reaberto com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fechamento não encontrado"),
            @ApiResponse(responseCode = "400", description = "Não é possível reabrir o fechamento"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<MonthlyClosingResponse> reopenClosing(
            @Parameter(description = "Ano de referência", example = "2025") @PathVariable int year,
            @Parameter(description = "Mês de referência", example = "10") @PathVariable int month,
            @Valid @RequestBody ReopenClosingRequest request,
            HttpServletRequest httpRequest) {
        
        YearMonth referenceMonth = YearMonth.of(year, month);
        MonthlyClosingResponse response = monthlyClosingService.reopenClosing(referenceMonth, request.getJustification());
        
        // Auditoria da reabertura
        String entityId = referenceMonth.toString();
        auditService.logReopen("MONTHLY_CLOSING", entityId, request.getJustification(), httpRequest);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{year}/{month}")
    @Operation(summary = "Obter fechamento por mês", description = "Retorna os detalhes de um fechamento mensal específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fechamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Fechamento não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<MonthlyClosingResponse> getClosingByMonth(
            @Parameter(description = "Ano de referência", example = "2025") @PathVariable int year,
            @Parameter(description = "Mês de referência", example = "10") @PathVariable int month) {
        
        YearMonth referenceMonth = YearMonth.of(year, month);
        MonthlyClosingResponse response = monthlyClosingService.getClosingByMonth(referenceMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os fechamentos", description = "Retorna uma lista de todos os fechamentos mensais ordenados por mês")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de fechamentos retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MonthlyClosingResponse>> getAllClosings() {
        List<MonthlyClosingResponse> closings = monthlyClosingService.getAllClosings();
        return ResponseEntity.ok(closings);
    }

    @GetMapping("/{year}/{month}/partners")
    @Operation(summary = "Obter resumo por parceiros", description = "Retorna o resumo do fechamento agrupado por parceiros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo por parceiros retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fechamento não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<PartnerClosingSummaryResponse>> getPartnerSummaries(
            @Parameter(description = "Ano de referência", example = "2025") @PathVariable int year,
            @Parameter(description = "Mês de referência", example = "10") @PathVariable int month) {
        
        YearMonth referenceMonth = YearMonth.of(year, month);
        List<PartnerClosingSummaryResponse> summaries = monthlyClosingService.getPartnerSummaries(referenceMonth);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar fechamentos por status", description = "Retorna fechamentos filtrados por status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de fechamentos retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MonthlyClosingResponse>> getClosingsByStatus(
            @Parameter(description = "Status do fechamento", example = "COMPLETED") @PathVariable String status) {
        
        // Implementar busca por status se necessário
        List<MonthlyClosingResponse> closings = monthlyClosingService.getAllClosings();
        return ResponseEntity.ok(closings);
    }
}

