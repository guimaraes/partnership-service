package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.PartnerDetailReportResponse;
import br.com.ecomercial.partnership.dto.PartnerReportResponse;
import br.com.ecomercial.partnership.entity.*;
import br.com.ecomercial.partnership.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final PartnerRepository partnerRepository;
    private final ClientRepository clientRepository;
    private final CommissionRuleRepository commissionRuleRepository;

    /**
     * Gera relatório consolidado por parceiros para um fechamento mensal
     */
    public List<PartnerReportResponse> generatePartnersReport(YearMonth referenceMonth) {
        List<Partner> partners = partnerRepository.findByStatus(Partner.Status.ACTIVE);

        return partners.stream()
                .map(partner -> {
                    // Para simplificar, vamos retornar dados básicos dos parceiros
                    return PartnerReportResponse.builder()
                            .partnerId(partner.getPartnerId())
                            .partnerName(partner.getName())
                            .partnerEmail(partner.getEmail())
                            .partnerPhone(partner.getPhone())
                            .partnerStatus(partner.getStatus().name())
                            .totalCommission(BigDecimal.ZERO) // Simplificado
                            .totalBonus(BigDecimal.ZERO) // Simplificado
                            .grandTotal(BigDecimal.ZERO) // Simplificado
                            .activeClients(0) // Simplificado
                            .totalClientBilling(BigDecimal.ZERO) // Simplificado
                            .reportDate(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Gera relatório detalhado de um parceiro específico
     */
    public PartnerDetailReportResponse generatePartnerDetailReport(YearMonth referenceMonth, String partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        List<Client> partnerClients = clientRepository.findByPartnerId(partnerId);

        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalBonus = BigDecimal.ZERO;
        int activeClients = 0;
        BigDecimal totalClientBilling = BigDecimal.ZERO;
        List<PartnerDetailReportResponse.ClientReportDetail> clientDetails = new ArrayList<>();

        for (Client client : partnerClients) {
            // Calcular comissão para o cliente
            BigDecimal clientCommission = calculateCommissionForClient(client, referenceMonth);
            BigDecimal clientBonus = BigDecimal.ZERO; // Simplificado por enquanto

            totalCommission = totalCommission.add(clientCommission);
            totalBonus = totalBonus.add(clientBonus);
            totalClientBilling = totalClientBilling.add(client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO);

            if (clientCommission.compareTo(BigDecimal.ZERO) > 0) {
                activeClients++;
            }

            clientDetails.add(PartnerDetailReportResponse.ClientReportDetail.builder()
                    .clientId(client.getId())
                    .clientName(client.getName())
                    .clientType(client.getClientType())
                    .clientStatus(client.getStatus())
                    .activeFrom(client.getCreatedAt().toLocalDate())
                    .inactiveFrom(client.getStatusEffectiveFrom())
                    .monthlyBilling(client.getMonthlyBilling())
                    .commissionValue(clientCommission)
                    .bonusValue(clientBonus)
                    .totalValue(clientCommission.add(clientBonus))
                    .observations(generateClientObservations(client, referenceMonth))
                    .build());
        }

        BigDecimal grandTotal = totalCommission.add(totalBonus);

        // Buscar políticas de bônus aplicadas (simplificado)
        List<PartnerDetailReportResponse.BonusPolicyApplied> bonusPolicies = new ArrayList<>();

        return PartnerDetailReportResponse.builder()
                .partnerId(partner.getPartnerId())
                .partnerName(partner.getName())
                .partnerEmail(partner.getEmail())
                .partnerPhone(partner.getPhone())
                .partnerStatus(partner.getStatus().name())
                .referenceMonth(referenceMonth.toString())
                .totalCommission(totalCommission)
                .totalBonus(totalBonus)
                .grandTotal(grandTotal)
                .activeClients(activeClients)
                .totalClientBilling(totalClientBilling)
                .reportDate(LocalDateTime.now())
                .clientDetails(clientDetails)
                .bonusPoliciesApplied(bonusPolicies)
                .build();
    }

    private BigDecimal calculateCommissionForClient(Client client, YearMonth referenceMonth) {
        try {
            CommissionRule rule = commissionRuleRepository
                    .findActiveRuleForClientTypeAndDate(client.getClientType(), referenceMonth.atDay(1))
                    .orElse(null);

            if (rule == null) {
                return BigDecimal.ZERO;
            }

            BigDecimal billing = client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO;

            // Verificar limites mínimos e máximos
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
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String generateClientObservations(Client client, YearMonth referenceMonth) {
        if (client.getStatus() == Client.Status.INACTIVE && client.getStatusEffectiveFrom() != null) {
            return "Cliente inativado em " + client.getStatusEffectiveFrom();
        }
        return "";
    }
}