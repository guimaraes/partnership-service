package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.PartnerRequest;
import br.com.ecomercial.partnership.dto.PartnerResponse;
import br.com.ecomercial.partnership.service.PartnerService;
import br.com.ecomercial.partnership.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Gestão de parceiros (contadores)")
@SecurityRequirement(name = "Bearer Authentication")
public class PartnerController {
    
    private final PartnerService partnerService;
    private final ValidationService validationService;
    
    @PostMapping
    @Operation(summary = "Criar parceiro", description = "Cria um novo parceiro no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parceiro criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PartnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Documento já existe")
    })
    public ResponseEntity<PartnerResponse> createPartner(@Valid @RequestBody PartnerRequest request) {
        // Validações de domínio
        validationService.validateEmailFormat(request.getEmail());
        validationService.validateDocumentFormat(request.getDocument());
        validationService.validateUniqueEmail(request.getEmail(), null);
        validationService.validateUniqueDocument(request.getDocument(), null);
        
        PartnerResponse response = partnerService.createPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Listar parceiros", description = "Retorna lista de todos os parceiros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de parceiros retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PartnerResponse.class)))
    })
    public ResponseEntity<List<PartnerResponse>> getAllPartners() {
        List<PartnerResponse> partners = partnerService.getAllPartners();
        return ResponseEntity.ok(partners);
    }
    
    @GetMapping("/{partnerId}")
    @Operation(summary = "Buscar parceiro por ID", description = "Retorna dados de um parceiro específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parceiro encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PartnerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    })
    public ResponseEntity<PartnerResponse> getPartnerById(
            @Parameter(description = "ID único do parceiro") @PathVariable String partnerId) {
        PartnerResponse partner = partnerService.getPartnerById(partnerId);
        return ResponseEntity.ok(partner);
    }
    
    @PutMapping("/{partnerId}")
    @Operation(summary = "Atualizar parceiro", description = "Atualiza dados de um parceiro existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parceiro atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PartnerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    })
    public ResponseEntity<PartnerResponse> updatePartner(
            @Parameter(description = "ID único do parceiro") @PathVariable String partnerId, 
            @Valid @RequestBody PartnerRequest request) {
        PartnerResponse response = partnerService.updatePartner(partnerId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{partnerId}")
    @Operation(summary = "Desativar parceiro", description = "Desativa um parceiro (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Parceiro desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Parceiro não encontrado"),
            @ApiResponse(responseCode = "409", description = "Parceiro possui clientes ativos")
    })
    public ResponseEntity<Void> deactivatePartner(
            @Parameter(description = "ID único do parceiro") @PathVariable String partnerId) {
        // Validação de negócio: não pode remover parceiro com clientes ativos
        validationService.validatePartnerDeletion(partnerId);
        
        partnerService.deactivatePartner(partnerId);
        return ResponseEntity.noContent().build();
    }
}
