package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.config.CustomHealthIndicator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Endpoints de saúde da aplicação")
public class HealthController {

    private final CustomHealthIndicator customHealthIndicator;

    @GetMapping
    @Operation(summary = "Verificar saúde da aplicação", description = "Retorna o status de saúde da aplicação com detalhes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aplicação saudável"),
            @ApiResponse(responseCode = "503", description = "Aplicação com problemas")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthStatus = customHealthIndicator.getHealthStatus();
        
        String status = (String) healthStatus.get("status");
        if ("UP".equals(status)) {
            return ResponseEntity.ok(healthStatus);
        } else {
            return ResponseEntity.status(503).body(healthStatus);
        }
    }

    @GetMapping("/simple")
    @Operation(summary = "Status simples", description = "Retorna apenas o status UP/DOWN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aplicação saudável"),
            @ApiResponse(responseCode = "503", description = "Aplicação com problemas")
    })
    public ResponseEntity<Map<String, String>> simpleHealth() {
        Map<String, Object> healthStatus = customHealthIndicator.getHealthStatus();
        String status = (String) healthStatus.get("status");
        
        Map<String, String> response = Map.of("status", status);
        
        if ("UP".equals(status)) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }
}
