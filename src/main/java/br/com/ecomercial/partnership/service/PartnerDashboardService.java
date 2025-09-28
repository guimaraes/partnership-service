package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.ClientDashboardDetail;
import br.com.ecomercial.partnership.dto.PartnerDashboardResponse;
import br.com.ecomercial.partnership.entity.*;
import br.com.ecomercial.partnership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerDashboardService {

    private final ClientRepository clientRepository;
    private final PartnerRepository partnerRepository;
    private final CommissionRuleRepository commissionRuleRepository;
    private final MonthlyClosingRepository monthlyClosingRepository;
    private final BonusCalculationService bonusCalculationService;

    /**
     * Obtém o dashboard do parceiro para um mês específico
     */
    public PartnerDashboardResponse getPartnerDashboard(YearMonth referenceMonth) {
        String partnerId = getCurrentPartnerId();
        log.info("Obtendo dashboard para parceiro {} no mês {}", partnerId, referenceMonth);

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        // Buscar clientes ativos do parceiro no período
        List<Client> activeClients = getActiveClientsForMonth(partnerId, referenceMonth);

        // Calcular comissões estimadas
        BigDecimal estimatedCommission = calculateEstimatedCommission(activeClients, referenceMonth);
        BigDecimal estimatedBonus = calculateEstimatedBonus(partnerId, referenceMonth);
        BigDecimal estimatedTotal = estimatedCommission.add(estimatedBonus);

        // Calcular faturamento total
        BigDecimal totalClientBilling = activeClients.stream()
                .filter(client -> client.getMonthlyBilling() != null)
                .map(Client::getMonthlyBilling)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageClientBilling = activeClients.isEmpty() ? BigDecimal.ZERO :
                totalClientBilling.divide(BigDecimal.valueOf(activeClients.size()), 2, BigDecimal.ROUND_HALF_UP);

        // Buscar último fechamento
        String lastClosing = findLastClosing();

        // Criar detalhes dos clientes
        List<ClientDashboardDetail> clientDetails = activeClients.stream()
                .map(client -> createClientDashboardDetail(client, referenceMonth))
                .collect(Collectors.toList());

        return PartnerDashboardResponse.builder()
                .partnerId(partnerId)
                .partnerName(partner.getName())
                .referenceMonth(referenceMonth.toString())
                .activeClients(activeClients.size())
                .estimatedCommission(estimatedCommission)
                .estimatedBonus(estimatedBonus)
                .estimatedTotal(estimatedTotal)
                .lastClosing(lastClosing)
                .partnerStatus(partner.getStatus().toString())
                .totalClientBilling(totalClientBilling)
                .averageClientBilling(averageClientBilling)
                .clientDetails(clientDetails)
                .build();
    }

    /**
     * Obtém apenas os detalhes dos clientes do parceiro para um mês específico
     */
    public List<ClientDashboardDetail> getPartnerClientDetails(YearMonth referenceMonth) {
        String partnerId = getCurrentPartnerId();
        log.info("Obtendo detalhes dos clientes para parceiro {} no mês {}", partnerId, referenceMonth);

        List<Client> activeClients = getActiveClientsForMonth(partnerId, referenceMonth);

        return activeClients.stream()
                .map(client -> createClientDashboardDetail(client, referenceMonth))
                .collect(Collectors.toList());
    }

    private List<Client> getActiveClientsForMonth(String partnerId, YearMonth referenceMonth) {
        LocalDate monthStart = referenceMonth.atDay(1);
        LocalDate monthEnd = referenceMonth.atEndOfMonth();

        return clientRepository.findByPartnerId(partnerId).stream()
                .filter(client -> isClientActiveInMonth(client, monthStart, monthEnd))
                .collect(Collectors.toList());
    }

    private boolean isClientActiveInMonth(Client client, LocalDate monthStart, LocalDate monthEnd) {
        // Cliente está ativo se:
        // 1. Status é ACTIVE e não há data de inativação
        // 2. Status é ACTIVE mas data de inativação é após o início do mês
        // 3. Status é INACTIVE mas data de inativação é após o início do mês

        if (client.getStatus() == Client.Status.ACTIVE) {
            return client.getStatusEffectiveFrom() == null || 
                   !client.getStatusEffectiveFrom().isBefore(monthStart);
        } else {
            return client.getStatusEffectiveFrom() != null && 
                   client.getStatusEffectiveFrom().isAfter(monthStart);
        }
    }

    private BigDecimal calculateEstimatedCommission(List<Client> clients, YearMonth referenceMonth) {
        LocalDate monthStart = referenceMonth.atDay(1);
        BigDecimal totalCommission = BigDecimal.ZERO;

        for (Client client : clients) {
            try {
                // Determinar tipo de cliente efetivo no mês
                Client.ClientType effectiveClientType = getEffectiveClientType(client, monthStart);
                
                // Buscar regra de comissão vigente
                Optional<CommissionRule> ruleOpt = commissionRuleRepository
                        .findActiveRuleForClientTypeAndDate(effectiveClientType, monthStart);

                if (ruleOpt.isPresent()) {
                    CommissionRule rule = ruleOpt.get();
                    BigDecimal commission = calculateCommissionValue(client, rule);
                    totalCommission = totalCommission.add(commission);
                }
            } catch (Exception e) {
                log.warn("Erro ao calcular comissão para cliente {}: {}", client.getId(), e.getMessage());
            }
        }

        return totalCommission;
    }

    private BigDecimal calculateEstimatedBonus(String partnerId, YearMonth referenceMonth) {
        try {
            return bonusCalculationService.calculateTotalBonusForPartner(partnerId, referenceMonth);
        } catch (Exception e) {
            log.warn("Erro ao calcular bônus para parceiro {}: {}", partnerId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private Client.ClientType getEffectiveClientType(Client client, LocalDate monthStart) {
        // Por simplicidade, usar o tipo atual do cliente
        // Em uma implementação mais complexa, seria necessário verificar o histórico de tipos
        return client.getClientType();
    }

    private BigDecimal calculateCommissionValue(Client client, CommissionRule rule) {
        BigDecimal billing = client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO;
        
        // Verificar thresholds
        if (rule.getMinBillingThreshold() != null && billing.compareTo(rule.getMinBillingThreshold()) < 0) {
            return BigDecimal.ZERO;
        }
        
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

    private String findLastClosing() {
        Optional<YearMonth> lastClosingOpt = monthlyClosingRepository.findLastCompletedMonth();
        return lastClosingOpt.map(YearMonth::toString).orElse(null);
    }

    private ClientDashboardDetail createClientDashboardDetail(Client client, YearMonth referenceMonth) {
        LocalDate monthStart = referenceMonth.atDay(1);
        LocalDate monthEnd = referenceMonth.atEndOfMonth();

        // Calcular comissão estimada para o cliente
        BigDecimal estimatedCommission = BigDecimal.ZERO;
        BigDecimal estimatedBonus = BigDecimal.ZERO;

        try {
            Client.ClientType effectiveClientType = getEffectiveClientType(client, monthStart);
            Optional<CommissionRule> ruleOpt = commissionRuleRepository
                    .findActiveRuleForClientTypeAndDate(effectiveClientType, monthStart);

            if (ruleOpt.isPresent()) {
                CommissionRule rule = ruleOpt.get();
                estimatedCommission = calculateCommissionValue(client, rule);
            }

            // Bônus simplificado baseado no faturamento individual
            BigDecimal billing = client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO;
            if (billing.compareTo(new BigDecimal("10000")) > 0) {
                estimatedBonus = billing.multiply(new BigDecimal("0.02")); // 2% bonus
            }
        } catch (Exception e) {
            log.warn("Erro ao calcular valores para cliente {}: {}", client.getId(), e.getMessage());
        }

        BigDecimal estimatedTotal = estimatedCommission.add(estimatedBonus);

        // Gerar observações
        String observations = generateClientObservations(client, monthStart, monthEnd);

        return ClientDashboardDetail.builder()
                .clientId(client.getId())
                .clientName(client.getName())
                .clientType(client.getClientType())
                .clientStatus(client.getStatus())
                .activeFrom(client.getCreatedAt() != null ? client.getCreatedAt().toLocalDate() : null)
                .inactiveFrom(client.getStatusEffectiveFrom())
                .monthlyBilling(client.getMonthlyBilling())
                .estimatedCommission(estimatedCommission)
                .estimatedBonus(estimatedBonus)
                .estimatedTotal(estimatedTotal)
                .observations(observations)
                .statusDescription(generateStatusDescription(client, monthStart, monthEnd))
                .build();
    }

    private String generateClientObservations(Client client, LocalDate monthStart, LocalDate monthEnd) {
        List<String> observations = new java.util.ArrayList<>();
        
        if (client.getStatusEffectiveFrom() != null && 
            client.getStatusEffectiveFrom().isAfter(monthStart) && 
            client.getStatusEffectiveFrom().isBefore(monthEnd)) {
            observations.add(String.format("Cliente inativado em %s", client.getStatusEffectiveFrom()));
        }
        
        return String.join("; ", observations);
    }

    private String generateStatusDescription(Client client, LocalDate monthStart, LocalDate monthEnd) {
        if (client.getStatusEffectiveFrom() != null && 
            client.getStatusEffectiveFrom().isAfter(monthStart) && 
            client.getStatusEffectiveFrom().isBefore(monthEnd)) {
            return String.format("Cliente inativado em %s", client.getStatusEffectiveFrom());
        }
        return "Cliente ativo durante todo o mês";
    }

    private String getCurrentPartnerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            
            if (user.getRole() == User.Role.PARTNER && user.getPartnerId() != null) {
                return user.getPartnerId();
            } else if (user.getRole() == User.Role.ADMIN) {
                // Admin: buscar o primeiro parceiro ativo
                return getFirstActivePartnerId();
            }
        }
        throw new RuntimeException("Partner ID not found in authentication context");
    }
    
    private String getFirstActivePartnerId() {
        // Buscar o primeiro parceiro ativo
        return partnerRepository.findByStatus(Partner.Status.ACTIVE)
                .stream()
                .findFirst()
                .map(Partner::getPartnerId)
                .orElseThrow(() -> new RuntimeException("No active partners found"));
    }
}