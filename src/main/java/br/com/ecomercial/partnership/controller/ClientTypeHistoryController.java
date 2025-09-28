package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.ClientTypeChangeRequest;
import br.com.ecomercial.partnership.dto.ClientTypeHistoryResponse;
import br.com.ecomercial.partnership.service.ClientTypeHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/type-history")
@RequiredArgsConstructor
@Tag(name = "Client Type History", description = "Gestão do histórico de tipos de cliente")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientTypeHistoryController {
    
    private final ClientTypeHistoryService clientTypeHistoryService;
    
    @GetMapping
    @Operation(summary = "Obter histórico de tipos", description = "Retorna o histórico completo de tipos de um cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico obtido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<List<ClientTypeHistoryResponse>> getClientTypeHistory(
            @Parameter(description = "ID do cliente") @PathVariable Long clientId) {
        List<ClientTypeHistoryResponse> history = clientTypeHistoryService.getClientTypeHistory(clientId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Obter tipo ativo para data", description = "Retorna o tipo ativo de um cliente em uma data específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo ativo encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado ou tipo não encontrado para a data")
    })
    public ResponseEntity<ClientTypeHistoryResponse> getActiveTypeForDate(
            @Parameter(description = "ID do cliente") @PathVariable Long clientId,
            @Parameter(description = "Data para consulta") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ClientTypeHistoryResponse activeType = clientTypeHistoryService.getActiveTypeForDate(clientId, date);
        return ResponseEntity.ok(activeType);
    }
    
    @PatchMapping
    @Operation(summary = "Alterar tipo de cliente", description = "Altera o tipo de um cliente com vigência a partir de uma data específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de datas de vigência")
    })
    public ResponseEntity<ClientTypeHistoryResponse> changeClientType(
            @Parameter(description = "ID do cliente") @PathVariable Long clientId,
            @Valid @RequestBody ClientTypeChangeRequest request) {
        ClientTypeHistoryResponse response = clientTypeHistoryService.changeClientType(clientId, request);
        return ResponseEntity.ok(response);
    }
}

