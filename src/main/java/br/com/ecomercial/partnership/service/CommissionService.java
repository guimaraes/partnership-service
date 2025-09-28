package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.CommissionResponse;
import br.com.ecomercial.partnership.entity.*;
import br.com.ecomercial.partnership.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommissionService {
    
    private final CommissionRepository commissionRepository;
    private final ClientRepository clientRepository;
    private final CommissionRuleRepository commissionRuleRepository;
    private final PartnerRepository partnerRepository;
    private final BonusCalculationService bonusCalculationService;
    
    public void calculateCommissionsForMonth(YearMonth referenceMonth) {
        List<Client> activeClients = clientRepository.findByStatus(Client.Status.ACTIVE);
        
        for (Client client : activeClients) {
            // Check if commission already exists for this month
            if (commissionRepository.findByPartnerIdAndClientIdAndReferenceMonth(
                    client.getPartnerId(), client.getId(), referenceMonth).isPresent()) {
                continue;
            }
            
            // Get commission rule for client type
            CommissionRule rule = commissionRuleRepository
                    .findByClientTypeAndStatus(client.getClientType(), CommissionRule.Status.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Commission rule not found for client type: " + client.getClientType()));
            
            // Calculate commission
            BigDecimal commissionValue = calculateCommissionValue(client, rule);
            BigDecimal bonusValue = calculateBonusValue(client, rule);
            BigDecimal totalValue = commissionValue.add(bonusValue != null ? bonusValue : BigDecimal.ZERO);
            
            // Create commission record
            Commission commission = Commission.builder()
                    .partnerId(client.getPartnerId())
                    .clientId(client.getId())
                    .referenceMonth(referenceMonth)
                    .clientBilling(client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO)
                    .commissionValue(commissionValue)
                    .bonusValue(bonusValue)
                    .totalValue(totalValue)
                    .status(Commission.Status.PENDING)
                    .build();
            
            commissionRepository.save(commission);
        }
    }
    
    @Transactional(readOnly = true)
    public List<CommissionResponse> getCommissionsByPartner(String partnerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this partner
        if (currentUser.getRole() == User.Role.PARTNER && 
            !partnerId.equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        return commissionRepository.findByPartnerId(partnerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CommissionResponse> getCommissionsByPartnerAndMonth(String partnerId, YearMonth referenceMonth) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this partner
        if (currentUser.getRole() == User.Role.PARTNER && 
            !partnerId.equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        return commissionRepository.findByPartnerIdAndReferenceMonth(partnerId, referenceMonth).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CommissionResponse> getAllCommissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Only ADMIN can see all commissions
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        
        return commissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public void approveCommission(Long commissionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Only ADMIN can approve commissions
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        
        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new RuntimeException("Commission not found"));
        
        commission.setStatus(Commission.Status.APPROVED);
        commissionRepository.save(commission);
    }
    
    public void payCommission(Long commissionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Only ADMIN can mark commissions as paid
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        
        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new RuntimeException("Commission not found"));
        
        if (commission.getStatus() != Commission.Status.APPROVED) {
            throw new RuntimeException("Commission must be approved before payment");
        }
        
        commission.setStatus(Commission.Status.PAID);
        commissionRepository.save(commission);
    }
    
    private BigDecimal calculateCommissionValue(Client client, CommissionRule rule) {
        BigDecimal billing = client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO;
        
        // Check if billing meets minimum threshold
        if (rule.getMinBillingThreshold() != null && billing.compareTo(rule.getMinBillingThreshold()) < 0) {
            return BigDecimal.ZERO;
        }
        
        // Check if billing exceeds maximum threshold
        if (rule.getMaxBillingThreshold() != null && billing.compareTo(rule.getMaxBillingThreshold()) > 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal fixedCommission = rule.getFixedCommission();
        BigDecimal percentageCommission = BigDecimal.ZERO;
        
        if (rule.getPercentageCommission() != null) {
            percentageCommission = billing.multiply(rule.getPercentageCommission());
        }
        
        return fixedCommission.add(percentageCommission);
    }
    
    private BigDecimal calculateBonusValue(Client client, CommissionRule rule) {
        // Simple bonus calculation for individual client billing
        BigDecimal billing = client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO;
        
        // Bonus for high billing clients (legacy calculation)
        if (billing.compareTo(new BigDecimal("10000")) > 0) {
            return billing.multiply(new BigDecimal("0.02")); // 2% bonus
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Calcula comissões incluindo bônus por desempenho para um mês específico
     */
    public void calculateCommissionsWithPerformanceBonusForMonth(YearMonth referenceMonth) {
        // Primeiro, calcular comissões normais
        calculateCommissionsForMonth(referenceMonth);
        
        // Depois, adicionar bônus por desempenho
        addPerformanceBonusesForMonth(referenceMonth);
    }

    /**
     * Adiciona bônus por desempenho às comissões de um mês específico
     */
    private void addPerformanceBonusesForMonth(YearMonth referenceMonth) {
        List<Partner> activePartners = partnerRepository.findByStatus(Partner.Status.ACTIVE);
        
        for (Partner partner : activePartners) {
            // Calcular bônus por desempenho para o parceiro
            BigDecimal performanceBonus = bonusCalculationService
                .calculateTotalBonusForPartner(partner.getPartnerId(), referenceMonth);
            
            if (performanceBonus.compareTo(BigDecimal.ZERO) > 0) {
                // Criar registro de comissão para o bônus de desempenho
                Commission performanceBonusCommission = Commission.builder()
                    .partnerId(partner.getPartnerId())
                    .clientId(null) // Bônus não está associado a um cliente específico
                    .referenceMonth(referenceMonth)
                    .clientBilling(BigDecimal.ZERO)
                    .commissionValue(BigDecimal.ZERO)
                    .bonusValue(performanceBonus)
                    .totalValue(performanceBonus)
                    .status(Commission.Status.PENDING)
                    .build();
                
                commissionRepository.save(performanceBonusCommission);
            }
        }
    }
    
    private CommissionResponse mapToResponse(Commission commission) {
        return CommissionResponse.builder()
                .id(commission.getId())
                .partnerId(commission.getPartnerId())
                .clientId(commission.getClientId())
                .referenceMonth(commission.getReferenceMonth())
                .clientBilling(commission.getClientBilling())
                .commissionValue(commission.getCommissionValue())
                .bonusValue(commission.getBonusValue())
                .totalValue(commission.getTotalValue())
                .status(commission.getStatus())
                .createdAt(commission.getCreatedAt())
                .updatedAt(commission.getUpdatedAt())
                .build();
    }
}
