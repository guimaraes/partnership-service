package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.entity.AuditLog;
import br.com.ecomercial.partnership.entity.User;
import br.com.ecomercial.partnership.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;

    /**
     * Registra uma ação de auditoria
     */
    @Transactional
    public void logAction(String entityType, String entityId, AuditLog.Action action, 
                         Object oldValue, Object newValue, String justification, 
                         HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                log.warn("No authenticated user found for audit log");
                return;
            }

            String oldValuesJson = serializeObject(oldValue);
            String newValuesJson = serializeObject(newValue);
            String ipAddress = getClientIpAddress(request);
            String userAgent = getUserAgent(request);

            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .userId(currentUser.getId().toString())
                    .userName(currentUser.getUsername())
                    .oldValues(oldValuesJson)
                    .newValues(newValuesJson)
                    .justification(justification)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} {} {} by {}", action, entityType, entityId, currentUser.getUsername());
            
            // Incrementar métrica de auditoria
            metricsService.incrementAuditLog(action.name());

        } catch (Exception e) {
            log.error("Error creating audit log", e);
        }
    }

    /**
     * Registra criação de entidade
     */
    public void logCreate(String entityType, String entityId, Object newValue, HttpServletRequest request) {
        logAction(entityType, entityId, AuditLog.Action.CREATE, null, newValue, null, request);
    }

    /**
     * Registra atualização de entidade
     */
    public void logUpdate(String entityType, String entityId, Object oldValue, Object newValue, HttpServletRequest request) {
        logAction(entityType, entityId, AuditLog.Action.UPDATE, oldValue, newValue, null, request);
    }

    /**
     * Registra exclusão de entidade
     */
    public void logDelete(String entityType, String entityId, Object oldValue, HttpServletRequest request) {
        logAction(entityType, entityId, AuditLog.Action.DELETE, oldValue, null, null, request);
    }

    /**
     * Registra reabertura de fechamento
     */
    public void logReopen(String entityType, String entityId, String justification, HttpServletRequest request) {
        logAction(entityType, entityId, AuditLog.Action.REOPEN, null, null, justification, request);
    }

    /**
     * Busca logs de auditoria por entidade
     */
    public List<AuditLog> getAuditLogsByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Busca logs de auditoria por tipo de entidade
     */
    public Page<AuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable);
    }

    /**
     * Busca logs de auditoria por usuário
     */
    public Page<AuditLog> getAuditLogsByUser(String userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Busca logs de auditoria por período
     */
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * Busca eventos de reabertura
     */
    public List<AuditLog> getReopenEvents() {
        return auditLogRepository.findReopenEvents();
    }

    /**
     * Busca logs de auditoria por ação
     */
    public List<AuditLog> getAuditLogsByAction(String entityType, AuditLog.Action action) {
        return auditLogRepository.findByEntityTypeAndAction(entityType, action);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    private String serializeObject(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Error serializing object for audit log", e);
            return obj.toString();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        return request.getHeader("User-Agent");
    }
}
