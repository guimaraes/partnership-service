package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.BonusCalculationResponse;
import br.com.ecomercial.partnership.entity.BonusPolicy;
import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.entity.Partner;
import br.com.ecomercial.partnership.repository.ClientRepository;
import br.com.ecomercial.partnership.repository.CommissionRepository;
import br.com.ecomercial.partnership.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BonusCalculationService {

    private final BonusPolicyService bonusPolicyService;
    private final ClientRepository clientRepository;
    private final CommissionRepository commissionRepository;
    private final PartnerRepository partnerRepository;

    public BonusCalculationResponse calculateBonusForPartner(String partnerId, YearMonth referenceMonth) {
        // Converter YearMonth para LocalDate (primeiro dia do mês)
        LocalDate calculationDate = referenceMonth.atDay(1);
        
        // Buscar clientes ativos do parceiro no período
        List<Client> activeClients = getActiveClientsForPeriod(partnerId, calculationDate);
        
        // Calcular estatísticas
        Integer activeClientsCount = activeClients.size();
        BigDecimal totalRevenue = calculateTotalRevenue(activeClients, referenceMonth);
        
        // Calcular bônus aplicáveis
        List<BonusCalculationResponse.BonusDetail> bonusDetails = calculateApplicableBonuses(
            calculationDate, activeClientsCount, totalRevenue);
        
        // Calcular total de bônus
        BigDecimal totalBonus = bonusDetails.stream()
            .map(BonusCalculationResponse.BonusDetail::getBonusAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return BonusCalculationResponse.builder()
            .partnerId(partnerId)
            .period(referenceMonth.toString())
            .activeClientsCount(activeClientsCount)
            .totalRevenue(totalRevenue)
            .totalBonus(totalBonus)
            .bonusDetails(bonusDetails)
            .build();
    }

    public BigDecimal calculateTotalBonusForPartner(String partnerId, YearMonth referenceMonth) {
        BonusCalculationResponse calculation = calculateBonusForPartner(partnerId, referenceMonth);
        return calculation.getTotalBonus();
    }

    private List<Client> getActiveClientsForPeriod(String partnerId, LocalDate calculationDate) {
        // Buscar todos os clientes do parceiro
        List<Client> partnerClients = clientRepository.findByPartnerId(partnerId);
        
        List<Client> activeClients = new ArrayList<>();
        
        for (Client client : partnerClients) {
            // Verificar se o cliente está ativo na data de cálculo
            if (isClientActiveOnDate(client, calculationDate)) {
                activeClients.add(client);
            }
        }
        
        return activeClients;
    }

    private boolean isClientActiveOnDate(Client client, LocalDate date) {
        // Cliente está ativo se:
        // 1. Status é ACTIVE E não há data de inativação definida
        // 2. Status é ACTIVE E a data de inativação é posterior à data de cálculo
        // 3. Status é INACTIVE mas a data de inativação é posterior à data de cálculo
        
        if (client.getStatus() == Client.Status.ACTIVE) {
            return client.getStatusEffectiveFrom() == null || 
                   !client.getStatusEffectiveFrom().isAfter(date);
        } else {
            return client.getStatusEffectiveFrom() != null && 
                   client.getStatusEffectiveFrom().isAfter(date);
        }
    }

    private BigDecimal calculateTotalRevenue(List<Client> clients, YearMonth referenceMonth) {
        // Para simplificar, vamos usar o monthlyBilling de cada cliente
        // Em um cenário real, isso viria de dados de faturamento específicos do mês
        return clients.stream()
            .map(client -> client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<BonusCalculationResponse.BonusDetail> calculateApplicableBonuses(
            LocalDate calculationDate, Integer clientCount, BigDecimal totalRevenue) {
        
        List<BonusCalculationResponse.BonusDetail> bonusDetails = new ArrayList<>();
        
        // Calcular bônus por número de clientes
        List<BonusPolicy> clientCountPolicies = bonusPolicyService
            .getEligibleClientCountPolicies(calculationDate, clientCount);
        
        for (BonusPolicy policy : clientCountPolicies) {
            bonusDetails.add(BonusCalculationResponse.BonusDetail.builder()
                .policyName(policy.getName())
                .type(policy.getType())
                .bonusAmount(policy.getBonusAmount())
                .description(String.format("%d clientes ativos (threshold: %d)", 
                    clientCount, policy.getThresholdClients()))
                .build());
        }
        
        // Calcular bônus por faturamento
        List<BonusPolicy> revenuePolicies = bonusPolicyService
            .getEligibleRevenuePolicies(calculationDate, totalRevenue);
        
        for (BonusPolicy policy : revenuePolicies) {
            bonusDetails.add(BonusCalculationResponse.BonusDetail.builder()
                .policyName(policy.getName())
                .type(policy.getType())
                .bonusAmount(policy.getBonusAmount())
                .description(String.format("R$ %.2f em faturamento (threshold: R$ %.2f)", 
                    totalRevenue.doubleValue(), policy.getRevenueThreshold().doubleValue()))
                .build());
        }
        
        return bonusDetails;
    }

    /**
     * Calcula bônus para todos os parceiros em um mês específico
     * Este método pode ser usado para processamento em lote
     */
    public List<BonusCalculationResponse> calculateBonusForAllPartners(YearMonth referenceMonth) {
        List<Partner> allPartners = partnerRepository.findByStatus(Partner.Status.ACTIVE);
        
        return allPartners.stream()
            .map(partner -> calculateBonusForPartner(partner.getPartnerId(), referenceMonth))
            .filter(calculation -> calculation.getTotalBonus().compareTo(BigDecimal.ZERO) > 0)
            .toList();
    }

    /**
     * Verifica se uma política de bônus é aplicável em uma data específica
     */
    public boolean isPolicyApplicableOnDate(BonusPolicy policy, LocalDate date) {
        return policy.isActive() && 
               !policy.getEffectiveFrom().isAfter(date) && 
               (policy.getEffectiveTo() == null || !policy.getEffectiveTo().isBefore(date));
    }
}
