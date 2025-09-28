package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.ClientRequest;
import br.com.ecomercial.partnership.dto.ClientResponse;
import br.com.ecomercial.partnership.dto.ClientStatusChangeRequest;
import br.com.ecomercial.partnership.service.ClientService;
import br.com.ecomercial.partnership.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partners/{partnerId}/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestão de clientes dos parceiros")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {
    
    private final ClientService clientService;
    private final ValidationService validationService;
    
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @PathVariable String partnerId, 
            @Valid @RequestBody ClientRequest request) {
        // Validações de domínio
        validationService.validateEmailFormat(request.getEmail());
        validationService.validateDocumentFormat(request.getDocument());
        
        ClientResponse response = clientService.createClient(partnerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ClientResponse>> getClientsByPartner(@PathVariable String partnerId) {
        List<ClientResponse> clients = clientService.getClientsByPartner(partnerId);
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long clientId) {
        ClientResponse client = clientService.getClientById(clientId);
        return ResponseEntity.ok(client);
    }
    
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long clientId, 
            @Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.updateClient(clientId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deactivateClient(@PathVariable Long clientId) {
        clientService.deactivateClient(clientId);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{clientId}/status")
    @Operation(summary = "Alterar status de cliente", description = "Altera o status de um cliente com vigência a partir de uma data específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ClientResponse> changeClientStatus(
            @Parameter(description = "ID do cliente") @PathVariable Long clientId,
            @Valid @RequestBody ClientStatusChangeRequest request) {
        ClientResponse response = clientService.changeClientStatus(clientId, request);
        return ResponseEntity.ok(response);
    }
}
