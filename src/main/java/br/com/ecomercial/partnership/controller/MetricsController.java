package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Métricas customizadas da aplicação")
@SecurityRequirement(name = "Bearer Authentication")
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/closings/execution-count")
    @Operation(summary = "Obter contador de execuções de fechamento", description = "Retorna o número de execuções de fechamento mensal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<Map<String, Object>> getClosingExecutionCount() {
        // Esta métrica é incrementada automaticamente pelo MetricsService
        // Aqui retornamos informações sobre a métrica
        Map<String, Object> response = new HashMap<>();
        response.put("metric", "closings.execution.count");
        response.put("description", "Number of monthly closing executions");
        response.put("note", "This metric is automatically incremented when monthly closings are executed");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/closings/test-increment")
    @Operation(summary = "Testar incremento de métrica de fechamento", description = "Incrementa a métrica de fechamento para teste")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica incrementada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<Map<String, Object>> testIncrementClosingMetric(
            @RequestParam(defaultValue = "2025-10") String month) {
        
        YearMonth referenceMonth = YearMonth.parse(month);
        metricsService.incrementClosingExecution(referenceMonth);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Closing execution metric incremented");
        response.put("month", referenceMonth.toString());
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Obter informações sobre métricas", description = "Retorna informações sobre as métricas disponíveis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<Map<String, Object>> getMetricsInfo() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, String> availableMetrics = new HashMap<>();
        availableMetrics.put("closings.execution.count", "Number of monthly closing executions");
        availableMetrics.put("closings.success.count", "Number of successful monthly closings");
        availableMetrics.put("closings.failure.count", "Number of failed monthly closings");
        availableMetrics.put("closings.reopen.count", "Number of monthly closing reopens");
        availableMetrics.put("closings.execution.duration", "Duration of monthly closing execution");
        availableMetrics.put("imports.partners.count", "Number of partners imported");
        availableMetrics.put("imports.clients.count", "Number of clients imported");
        availableMetrics.put("imports.success.count", "Number of successful imports");
        availableMetrics.put("imports.failure.count", "Number of failed imports");
        availableMetrics.put("audit.logs.count", "Number of audit logs created");
        availableMetrics.put("partners.created.count", "Number of partners created");
        availableMetrics.put("partners.updated.count", "Number of partners updated");
        availableMetrics.put("partners.deleted.count", "Number of partners deleted");
        availableMetrics.put("clients.created.count", "Number of clients created");
        availableMetrics.put("clients.updated.count", "Number of clients updated");
        availableMetrics.put("clients.deleted.count", "Number of clients deleted");
        availableMetrics.put("commission.rules.created.count", "Number of commission rules created");
        availableMetrics.put("commission.rules.updated.count", "Number of commission rules updated");
        
        response.put("available_metrics", availableMetrics);
        response.put("prometheus_endpoint", "/actuator/prometheus");
        response.put("health_endpoint", "/actuator/health");
        response.put("info_endpoint", "/actuator/info");
        
        return ResponseEntity.ok(response);
    }
}
