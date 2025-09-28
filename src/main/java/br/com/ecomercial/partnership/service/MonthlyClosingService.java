package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.*;
import br.com.ecomercial.partnership.entity.*;
import br.com.ecomercial.partnership.repository.*;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyClosingService {

    private final MonthlyClosingRepository monthlyClosingRepository;
    private final ClosingDetailRepository closingDetailRepository;
    private final ClientRepository clientRepository;
    private final CommissionRuleRepository commissionRuleRepository;
    private final PartnerRepository partnerRepository;
    private final BonusCalculationService bonusCalculationService;
    private final MetricsService metricsService;

    /**
     * Executa o fechamento mensal para um mês específico
     */
    public MonthlyClosingResponse runMonthlyClosing(YearMonth referenceMonth) {
        log.info("Iniciando fechamento mensal para: {}", referenceMonth);

        // Incrementar métrica de execução de fechamento
        metricsService.incrementClosingExecution(referenceMonth);
        
        // Iniciar timer de execução
        Timer.Sample sample = metricsService.startClosingTimer();

        try {
            // Verificar se já existe fechamento para o mês
            if (monthlyClosingRepository.existsByReferenceMonth(referenceMonth)) {
                MonthlyClosing existingClosing = monthlyClosingRepository.findByReferenceMonth(referenceMonth)
                        .orElseThrow(() -> new RuntimeException("Monthly closing not found"));
                
                if (existingClosing.isCompleted()) {
                    throw new RuntimeException("Closing already exists for month: " + referenceMonth);
                }
            }

        // Criar ou atualizar fechamento
        MonthlyClosing closing = createOrUpdateClosing(referenceMonth);

        // Calcular comissões e bônus
        List<ClosingDetail> closingDetails = calculateClosingDetails(referenceMonth, closing);

        // Atualizar totais do fechamento
        updateClosingTotals(closing, closingDetails);

        // Finalizar fechamento
        closing.setStatus(MonthlyClosing.Status.COMPLETED);
        closing.setClosedBy(getCurrentUsername());
        MonthlyClosing savedClosing = monthlyClosingRepository.save(closing);

        log.info("Fechamento mensal concluído para: {} com {} detalhes", referenceMonth, closingDetails.size());

        // Incrementar métrica de sucesso e parar timer
        metricsService.incrementClosingSuccess(referenceMonth);
        metricsService.stopClosingTimer(sample);

        return mapToResponse(savedClosing);
        
        } catch (Exception e) {
            // Incrementar métrica de falha e parar timer
            metricsService.incrementClosingFailure(referenceMonth, e.getMessage());
            metricsService.stopClosingTimer(sample);
            throw e;
        }
    }

    /**
     * Reabre um fechamento para correções
     */
    public MonthlyClosingResponse reopenClosing(YearMonth referenceMonth, String justification) {
        MonthlyClosing closing = monthlyClosingRepository.findByReferenceMonth(referenceMonth)
                .orElseThrow(() -> new RuntimeException("Monthly closing not found for month: " + referenceMonth));

        if (!closing.canBeReopened()) {
            throw new RuntimeException("Cannot reopen closing with status: " + closing.getStatus());
        }

        closing.setStatus(MonthlyClosing.Status.REOPENED);
        closing.setJustification(justification);
        closing.setReopenedBy(getCurrentUsername());

        MonthlyClosing savedClosing = monthlyClosingRepository.save(closing);
        log.info("Fechamento reaberto para: {} com justificativa: {}", referenceMonth, justification);

        // Incrementar métrica de reabertura
        metricsService.incrementClosingReopen(referenceMonth);

        return mapToResponse(savedClosing);
    }

    /**
     * Obtém um fechamento por mês
     */
    @Transactional(readOnly = true)
    public MonthlyClosingResponse getClosingByMonth(YearMonth referenceMonth) {
        MonthlyClosing closing = monthlyClosingRepository.findByReferenceMonth(referenceMonth)
                .orElseThrow(() -> new RuntimeException("Monthly closing not found for month: " + referenceMonth));

        return mapToResponse(closing);
    }

    /**
     * Lista todos os fechamentos
     */
    @Transactional(readOnly = true)
    public List<MonthlyClosingResponse> getAllClosings() {
        return monthlyClosingRepository.findAllOrderByReferenceMonthDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtém resumo do fechamento por parceiro
     */
    @Transactional(readOnly = true)
    public List<PartnerClosingSummaryResponse> getPartnerSummaries(YearMonth referenceMonth) {
        MonthlyClosing closing = monthlyClosingRepository.findByReferenceMonth(referenceMonth)
                .orElseThrow(() -> new RuntimeException("Monthly closing not found for month: " + referenceMonth));

        List<ClosingDetail> details = closingDetailRepository.findByMonthlyClosing(closing);
        
        return details.stream()
                .collect(Collectors.groupingBy(ClosingDetail::getPartnerId))
                .entrySet().stream()
                .map(entry -> createPartnerSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private MonthlyClosing createOrUpdateClosing(YearMonth referenceMonth) {
        Optional<MonthlyClosing> existingClosing = monthlyClosingRepository.findByReferenceMonth(referenceMonth);
        
        if (existingClosing.isPresent()) {
            MonthlyClosing closing = existingClosing.get();
            closing.setStatus(MonthlyClosing.Status.IN_PROGRESS);
            return closing;
        } else {
            return MonthlyClosing.builder()
                    .referenceMonth(referenceMonth)
                    .status(MonthlyClosing.Status.IN_PROGRESS)
                    .totalPartners(0)
                    .totalClients(0)
                    .totalCommission(BigDecimal.ZERO)
                    .totalBonus(BigDecimal.ZERO)
                    .totalPayout(BigDecimal.ZERO)
                    .build();
        }
    }

    private List<ClosingDetail> calculateClosingDetails(YearMonth referenceMonth, MonthlyClosing closing) {
        List<ClosingDetail> details = new ArrayList<>();
        LocalDate monthStart = referenceMonth.atDay(1);
        LocalDate monthEnd = referenceMonth.atEndOfMonth();

        // Buscar todos os clientes ativos no período
        List<Client> activeClients = clientRepository.findAll().stream()
                .filter(client -> isClientActiveInMonth(client, monthStart, monthEnd))
                .collect(Collectors.toList());

        for (Client client : activeClients) {
            try {
                ClosingDetail detail = calculateClientClosingDetail(client, referenceMonth, closing);
                details.add(detail);
            } catch (Exception e) {
                log.error("Erro ao calcular detalhe para cliente {}: {}", client.getId(), e.getMessage());
            }
        }

        // Salvar todos os detalhes
        closingDetailRepository.saveAll(details);
        
        return details;
    }

    private ClosingDetail calculateClientClosingDetail(Client client, YearMonth referenceMonth, MonthlyClosing closing) {
        LocalDate monthStart = referenceMonth.atDay(1);
        LocalDate monthEnd = referenceMonth.atEndOfMonth();

        // Determinar tipo de cliente efetivo no mês
        Client.ClientType effectiveClientType = getEffectiveClientType(client, monthStart);
        
        // Buscar regra de comissão vigente
        CommissionRule rule = commissionRuleRepository
                .findActiveRuleForClientTypeAndDate(effectiveClientType, monthStart)
                .orElseThrow(() -> new RuntimeException("Commission rule not found for client type: " + effectiveClientType));

        // Calcular comissão
        BigDecimal commissionValue = calculateCommissionValue(client, rule);
        
        // Calcular bônus (simplificado - apenas bônus por faturamento individual)
        BigDecimal bonusValue = calculateClientBonus(client, rule);

        // Determinar observações
        String observations = generateObservations(client, monthStart, monthEnd);

        return ClosingDetail.builder()
                .monthlyClosing(closing)
                .partnerId(client.getPartnerId())
                .clientId(client.getId())
                .clientName(client.getName())
                .clientType(effectiveClientType)
                .clientStatus(client.getStatus())
                .clientActiveFrom(client.getCreatedAt() != null ? client.getCreatedAt().toLocalDate() : null)
                .clientInactiveFrom(client.getStatusEffectiveFrom())
                .monthlyRevenue(client.getMonthlyBilling())
                .ruleType(effectiveClientType)
                .commissionRate(rule.getPercentageCommission())
                .commissionValue(commissionValue)
                .bonusValue(bonusValue)
                .totalValue(commissionValue.add(bonusValue))
                .observations(observations)
                .build();
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

    private BigDecimal calculateClientBonus(Client client, CommissionRule rule) {
        // Bônus simplificado baseado no faturamento individual
        BigDecimal billing = client.getMonthlyBilling() != null ? client.getMonthlyBilling() : BigDecimal.ZERO;
        
        if (billing.compareTo(new BigDecimal("10000")) > 0) {
            return billing.multiply(new BigDecimal("0.02")); // 2% bonus
        }
        
        return BigDecimal.ZERO;
    }

    private String generateObservations(Client client, LocalDate monthStart, LocalDate monthEnd) {
        List<String> observations = new ArrayList<>();
        
        if (client.getStatusEffectiveFrom() != null && 
            client.getStatusEffectiveFrom().isAfter(monthStart) && 
            client.getStatusEffectiveFrom().isBefore(monthEnd)) {
            observations.add(String.format("Cliente inativado em %s", client.getStatusEffectiveFrom()));
        }
        
        return String.join("; ", observations);
    }

    private void updateClosingTotals(MonthlyClosing closing, List<ClosingDetail> details) {
        closing.setTotalClients(details.size());
        
        closing.setTotalCommission(details.stream()
                .map(ClosingDetail::getCommissionValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        closing.setTotalBonus(details.stream()
                .map(ClosingDetail::getBonusValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        closing.setTotalPayout(details.stream()
                .map(ClosingDetail::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        closing.setTotalPartners((int) details.stream()
                .map(ClosingDetail::getPartnerId)
                .distinct()
                .count());
    }

    private PartnerClosingSummaryResponse createPartnerSummary(String partnerId, List<ClosingDetail> details) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElse(null);
        
        String partnerName = partner != null ? partner.getName() : "Parceiro não encontrado";
        
        BigDecimal totalCommission = details.stream()
                .map(ClosingDetail::getCommissionValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalBonus = details.stream()
                .map(ClosingDetail::getBonusValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPayout = details.stream()
                .map(ClosingDetail::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return PartnerClosingSummaryResponse.builder()
                .partnerId(partnerId)
                .partnerName(partnerName)
                .totalClients(details.size())
                .totalCommission(totalCommission)
                .totalBonus(totalBonus)
                .totalPayout(totalPayout)
                .clientDetails(details.stream()
                        .map(this::mapToDetailResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return ((User) auth.getPrincipal()).getUsername();
        }
        return "system";
    }

    private MonthlyClosingResponse mapToResponse(MonthlyClosing closing) {
        List<ClosingDetail> details = closingDetailRepository.findByMonthlyClosing(closing);
        
        return MonthlyClosingResponse.builder()
                .id(closing.getId())
                .referenceMonth(closing.getReferenceMonth().toString())
                .status(closing.getStatus())
                .totalPartners(closing.getTotalPartners())
                .totalClients(closing.getTotalClients())
                .totalCommission(closing.getTotalCommission())
                .totalBonus(closing.getTotalBonus())
                .totalPayout(closing.getTotalPayout())
                .justification(closing.getJustification())
                .closedBy(closing.getClosedBy())
                .reopenedBy(closing.getReopenedBy())
                .createdAt(closing.getCreatedAt())
                .updatedAt(closing.getUpdatedAt())
                .closingDetails(details.stream()
                        .map(this::mapToDetailResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private ClosingDetailResponse mapToDetailResponse(ClosingDetail detail) {
        return ClosingDetailResponse.builder()
                .id(detail.getId())
                .partnerId(detail.getPartnerId())
                .clientId(detail.getClientId())
                .clientName(detail.getClientName())
                .clientType(detail.getClientType())
                .clientStatus(detail.getClientStatus())
                .clientActiveFrom(detail.getClientActiveFrom())
                .clientInactiveFrom(detail.getClientInactiveFrom())
                .monthlyRevenue(detail.getMonthlyRevenue())
                .ruleType(detail.getRuleType())
                .commissionRate(detail.getCommissionRate())
                .commissionValue(detail.getCommissionValue())
                .bonusValue(detail.getBonusValue())
                .totalValue(detail.getTotalValue())
                .observations(detail.getObservations())
                .statusDescription(detail.getStatusDescription())
                .createdAt(detail.getCreatedAt())
                .build();
    }
}

