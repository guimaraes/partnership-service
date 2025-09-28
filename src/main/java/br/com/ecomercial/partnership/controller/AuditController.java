package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.AuditLogResponse;
import br.com.ecomercial.partnership.entity.AuditLog;
import br.com.ecomercial.partnership.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Logs de auditoria e trilhas")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Buscar logs de auditoria por entidade", description = "Retorna logs de auditoria para uma entidade específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs de auditoria retornados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByEntity(
            @Parameter(description = "Tipo da entidade", example = "COMMISSION_RULE") @PathVariable String entityType,
            @Parameter(description = "ID da entidade", example = "CR-1001") @PathVariable String entityId) {

        validateAdminAccess();

        List<AuditLog> auditLogs = auditService.getAuditLogsByEntity(entityType, entityId);
        List<AuditLogResponse> responses = auditLogs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/entity-type/{entityType}")
    @Operation(summary = "Buscar logs de auditoria por tipo de entidade", description = "Retorna logs de auditoria para um tipo de entidade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs de auditoria retornados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByEntityType(
            @Parameter(description = "Tipo da entidade", example = "COMMISSION_RULE") @PathVariable String entityType,
            @Parameter(description = "Número da página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20") @RequestParam(defaultValue = "20") int size) {

        validateAdminAccess();

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAuditLogsByEntityType(entityType, pageable);
        Page<AuditLogResponse> responses = auditLogs.map(this::convertToResponse);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar logs de auditoria por usuário", description = "Retorna logs de auditoria para um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs de auditoria retornados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByUser(
            @Parameter(description = "ID do usuário", example = "1") @PathVariable String userId,
            @Parameter(description = "Número da página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20") @RequestParam(defaultValue = "20") int size) {

        validateAdminAccess();

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAuditLogsByUser(userId, pageable);
        Page<AuditLogResponse> responses = auditLogs.map(this::convertToResponse);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Buscar logs de auditoria por período", description = "Retorna logs de auditoria em um período específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs de auditoria retornados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByDateRange(
            @Parameter(description = "Data inicial (yyyy-MM-ddTHH:mm:ss)", example = "2025-10-01T00:00:00") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Data final (yyyy-MM-ddTHH:mm:ss)", example = "2025-10-31T23:59:59") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Número da página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20") @RequestParam(defaultValue = "20") int size) {

        validateAdminAccess();

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(startDate, endDate, pageable);
        Page<AuditLogResponse> responses = auditLogs.map(this::convertToResponse);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reopen-events")
    @Operation(summary = "Buscar eventos de reabertura", description = "Retorna todos os eventos de reabertura de fechamentos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eventos de reabertura retornados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<List<AuditLogResponse>> getReopenEvents() {

        validateAdminAccess();

        List<AuditLog> auditLogs = auditService.getReopenEvents();
        List<AuditLogResponse> responses = auditLogs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/action/{entityType}/{action}")
    @Operation(summary = "Buscar logs de auditoria por ação", description = "Retorna logs de auditoria para uma ação específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs de auditoria retornados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Requer ADMIN role")
    })
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByAction(
            @Parameter(description = "Tipo da entidade", example = "COMMISSION_RULE") @PathVariable String entityType,
            @Parameter(description = "Ação", example = "CREATE") @PathVariable String action) {

        validateAdminAccess();

        AuditLog.Action actionEnum = AuditLog.Action.valueOf(action.toUpperCase());
        List<AuditLog> auditLogs = auditService.getAuditLogsByAction(entityType, actionEnum);
        List<AuditLogResponse> responses = auditLogs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private void validateAdminAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof br.com.ecomercial.partnership.entity.User)) {
            throw new RuntimeException("Authentication required");
        }
        
        br.com.ecomercial.partnership.entity.User user = (br.com.ecomercial.partnership.entity.User) auth.getPrincipal();
        if (user.getRole() != br.com.ecomercial.partnership.entity.User.Role.ADMIN) {
            throw new RuntimeException("Admin access required for audit logs");
        }
    }

    private AuditLogResponse convertToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction().name())
                .userId(auditLog.getUserId())
                .userName(auditLog.getUserName())
                .oldValues(auditLog.getOldValues())
                .newValues(auditLog.getNewValues())
                .justification(auditLog.getJustification())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
