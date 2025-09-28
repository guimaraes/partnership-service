package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.ImportResponse;
import br.com.ecomercial.partnership.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
@Tag(name = "Import", description = "Importação em massa de dados")
@SecurityRequirement(name = "Bearer Authentication")
public class ImportController {

    private final ImportService importService;

    @PostMapping("/partners")
    @Operation(summary = "Importar parceiros via CSV", description = "Inicia importação assíncrona de parceiros a partir de arquivo CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Importação aceita e iniciada"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou formato incorreto"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<ImportResponse> importPartners(
            @Parameter(description = "Arquivo CSV com dados dos parceiros") 
            @RequestParam("file") MultipartFile file) {

        validateAdminAccess();
        validateCsvFile(file);

        ImportResponse response = importService.importPartners(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/clients")
    @Operation(summary = "Importar clientes via CSV", description = "Inicia importação assíncrona de clientes a partir de arquivo CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Importação aceita e iniciada"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou formato incorreto"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<ImportResponse> importClients(
            @Parameter(description = "Arquivo CSV com dados dos clientes") 
            @RequestParam("file") MultipartFile file) {

        validateAdminAccess();
        validateCsvFile(file);

        ImportResponse response = importService.importClients(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/status/{jobId}")
    @Operation(summary = "Consultar status de importação", description = "Retorna o status atual de um job de importação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Job não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<ImportResponse> getImportStatus(
            @Parameter(description = "ID do job de importação", example = "import-12345")
            @PathVariable String jobId) {

        validateAdminAccess();

        ImportResponse response = importService.getImportStatus(jobId);
        return ResponseEntity.ok(response);
    }

    private void validateAdminAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof br.com.ecomercial.partnership.entity.User)) {
            throw new RuntimeException("Authentication required");
        }
        
        br.com.ecomercial.partnership.entity.User user = (br.com.ecomercial.partnership.entity.User) auth.getPrincipal();
        if (user.getRole() != br.com.ecomercial.partnership.entity.User.Role.ADMIN) {
            throw new RuntimeException("Admin access required for imports");
        }
    }

    private void validateCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Arquivo não pode ser vazio");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new RuntimeException("Arquivo deve ser do tipo CSV");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new RuntimeException("Arquivo muito grande. Tamanho máximo: 10MB");
        }
    }
}
