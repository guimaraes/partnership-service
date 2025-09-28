package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.CommissionRuleRequest;
import br.com.ecomercial.partnership.dto.CommissionRuleResponse;
import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.entity.CommissionRule;
import br.com.ecomercial.partnership.repository.CommissionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommissionRuleService {
    
    private final CommissionRuleRepository commissionRuleRepository;
    private final MetricsService metricsService;
    
    public CommissionRuleResponse createCommissionRule(CommissionRuleRequest request) {
        // Validar se a data de vigência é no futuro
        if (request.getEffectiveFrom().isBefore(LocalDate.now())) {
            throw new RuntimeException("Effective from date must be in the future");
        }
        
        // Verificar sobreposição de períodos
        if (commissionRuleRepository.existsOverlappingRule(request.getClientType(), 
                request.getEffectiveFrom(), request.getEffectiveTo())) {
            throw new RuntimeException("Overlapping effective period for client type " + request.getClientType());
        }
        
        CommissionRule rule = CommissionRule.builder()
                .clientType(request.getClientType())
                .fixedCommission(request.getFixedCommission())
                .percentageCommission(request.getPercentageCommission())
                .minBillingThreshold(request.getMinBillingThreshold())
                .maxBillingThreshold(request.getMaxBillingThreshold())
                .status(CommissionRule.Status.ACTIVE)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();
        
        CommissionRule savedRule = commissionRuleRepository.save(rule);
        
        // Incrementar métrica de criação de regra de comissão
        metricsService.incrementCommissionRuleCreated();
        
        return mapToResponse(savedRule);
    }
    
    @Transactional(readOnly = true)
    public List<CommissionRuleResponse> getAllCommissionRules() {
        return commissionRuleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CommissionRuleResponse getCommissionRuleById(Long id) {
        CommissionRule rule = commissionRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission rule not found"));
        
        return mapToResponse(rule);
    }
    
    @Transactional(readOnly = true)
    public CommissionRuleResponse getCommissionRuleByClientType(Client.ClientType clientType) {
        CommissionRule rule = commissionRuleRepository.findActiveRuleForClientTypeAndDate(clientType, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("Commission rule not found for client type: " + clientType));
        
        return mapToResponse(rule);
    }
    
    @Transactional(readOnly = true)
    public CommissionRuleResponse getCommissionRuleByClientTypeAndDate(Client.ClientType clientType, LocalDate date) {
        CommissionRule rule = commissionRuleRepository.findActiveRuleForClientTypeAndDate(clientType, date)
                .orElseThrow(() -> new RuntimeException("Commission rule not found for client type: " + clientType + " on date: " + date));
        
        return mapToResponse(rule);
    }
    
    @Transactional(readOnly = true)
    public List<CommissionRuleResponse> getCommissionRulesForClientType(Client.ClientType clientType) {
        return commissionRuleRepository.findAllActiveRulesForClientType(clientType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public CommissionRuleResponse updateCommissionRule(Long id, CommissionRuleRequest request) {
        CommissionRule rule = commissionRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission rule not found"));
        
        // Check if client type is unique (excluding current rule)
        if (!rule.getClientType().equals(request.getClientType()) && 
            commissionRuleRepository.existsByClientType(request.getClientType())) {
            throw new RuntimeException("Commission rule for this client type already exists");
        }
        
        rule.setClientType(request.getClientType());
        rule.setFixedCommission(request.getFixedCommission());
        rule.setPercentageCommission(request.getPercentageCommission());
        rule.setMinBillingThreshold(request.getMinBillingThreshold());
        rule.setMaxBillingThreshold(request.getMaxBillingThreshold());
        
        CommissionRule updatedRule = commissionRuleRepository.save(rule);
        
        // Incrementar métrica de atualização de regra de comissão
        metricsService.incrementCommissionRuleUpdated();
        
        return mapToResponse(updatedRule);
    }
    
    public void deactivateCommissionRule(Long id) {
        CommissionRule rule = commissionRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission rule not found"));
        
        rule.setStatus(CommissionRule.Status.INACTIVE);
        commissionRuleRepository.save(rule);
    }
    
    private CommissionRuleResponse mapToResponse(CommissionRule rule) {
        return CommissionRuleResponse.builder()
                .id(rule.getId())
                .clientType(rule.getClientType())
                .fixedCommission(rule.getFixedCommission())
                .percentageCommission(rule.getPercentageCommission())
                .minBillingThreshold(rule.getMinBillingThreshold())
                .maxBillingThreshold(rule.getMaxBillingThreshold())
                .status(rule.getStatus())
                .effectiveFrom(rule.getEffectiveFrom())
                .effectiveTo(rule.getEffectiveTo())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
