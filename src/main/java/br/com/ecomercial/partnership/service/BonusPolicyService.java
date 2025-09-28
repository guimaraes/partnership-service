package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.BonusPolicyRequest;
import br.com.ecomercial.partnership.dto.BonusPolicyResponse;
import br.com.ecomercial.partnership.entity.BonusPolicy;
import br.com.ecomercial.partnership.repository.BonusPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BonusPolicyService {

    private final BonusPolicyRepository bonusPolicyRepository;

    public BonusPolicyResponse createBonusPolicy(BonusPolicyRequest request) {
        // Validar se o nome já existe
        if (bonusPolicyRepository.existsByName(request.getName())) {
            throw new RuntimeException("Bonus policy with name " + request.getName() + " already exists");
        }

        // Validar se a data de vigência é no futuro
        if (request.getEffectiveFrom().isBefore(LocalDate.now())) {
            throw new RuntimeException("Effective from date must be in the future");
        }

        // Validar se pelo menos um threshold é definido baseado no tipo
        validatePolicyThresholds(request);

        BonusPolicy policy = BonusPolicy.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .thresholdClients(request.getThresholdClients())
                .revenueThreshold(request.getRevenueThreshold())
                .bonusAmount(request.getBonusAmount())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .status(BonusPolicy.Status.ACTIVE)
                .build();

        BonusPolicy savedPolicy = bonusPolicyRepository.save(policy);
        return mapToResponse(savedPolicy);
    }

    @Transactional(readOnly = true)
    public List<BonusPolicyResponse> getAllBonusPolicies() {
        return bonusPolicyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BonusPolicyResponse getBonusPolicyById(Long id) {
        BonusPolicy policy = bonusPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bonus policy not found with id: " + id));
        return mapToResponse(policy);
    }

    @Transactional(readOnly = true)
    public BonusPolicyResponse getBonusPolicyByName(String name) {
        BonusPolicy policy = bonusPolicyRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Bonus policy not found with name: " + name));
        return mapToResponse(policy);
    }

    public BonusPolicyResponse updateBonusPolicy(Long id, BonusPolicyRequest request) {
        BonusPolicy policy = bonusPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bonus policy not found with id: " + id));

        // Validar se o nome já existe (excluindo o próprio registro)
        if (bonusPolicyRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Bonus policy with name " + request.getName() + " already exists");
        }

        // Validar thresholds
        validatePolicyThresholds(request);

        policy.setName(request.getName());
        policy.setDescription(request.getDescription());
        policy.setType(request.getType());
        policy.setThresholdClients(request.getThresholdClients());
        policy.setRevenueThreshold(request.getRevenueThreshold());
        policy.setBonusAmount(request.getBonusAmount());
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setEffectiveTo(request.getEffectiveTo());

        BonusPolicy updatedPolicy = bonusPolicyRepository.save(policy);
        return mapToResponse(updatedPolicy);
    }

    public void deactivateBonusPolicy(Long id) {
        BonusPolicy policy = bonusPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bonus policy not found with id: " + id));

        policy.setStatus(BonusPolicy.Status.INACTIVE);
        bonusPolicyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public List<BonusPolicyResponse> getActiveBonusPoliciesOnDate(LocalDate date) {
        return bonusPolicyRepository.findActivePoliciesOnDate(date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BonusPolicyResponse> getActiveBonusPoliciesByTypeOnDate(BonusPolicy.BonusType type, LocalDate date) {
        return bonusPolicyRepository.findActivePoliciesByTypeOnDate(type, date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BonusPolicy> getEligibleClientCountPolicies(LocalDate date, Integer clientCount) {
        return bonusPolicyRepository.findEligibleClientCountPolicies(date, clientCount);
    }

    @Transactional(readOnly = true)
    public List<BonusPolicy> getEligibleRevenuePolicies(LocalDate date, java.math.BigDecimal revenue) {
        return bonusPolicyRepository.findEligibleRevenuePolicies(date, revenue);
    }

    private void validatePolicyThresholds(BonusPolicyRequest request) {
        if (request.getType() == BonusPolicy.BonusType.CLIENT_COUNT && request.getThresholdClients() == null) {
            throw new RuntimeException("Threshold clients is required for CLIENT_COUNT type");
        }

        if (request.getType() == BonusPolicy.BonusType.REVENUE_THRESHOLD && request.getRevenueThreshold() == null) {
            throw new RuntimeException("Revenue threshold is required for REVENUE_THRESHOLD type");
        }

        if (request.getType() == BonusPolicy.BonusType.CLIENT_COUNT && request.getRevenueThreshold() != null) {
            throw new RuntimeException("Revenue threshold should not be set for CLIENT_COUNT type");
        }

        if (request.getType() == BonusPolicy.BonusType.REVENUE_THRESHOLD && request.getThresholdClients() != null) {
            throw new RuntimeException("Threshold clients should not be set for REVENUE_THRESHOLD type");
        }
    }

    private BonusPolicyResponse mapToResponse(BonusPolicy policy) {
        return BonusPolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .description(policy.getDescription())
                .type(policy.getType())
                .thresholdClients(policy.getThresholdClients())
                .revenueThreshold(policy.getRevenueThreshold())
                .bonusAmount(policy.getBonusAmount())
                .effectiveFrom(policy.getEffectiveFrom())
                .effectiveTo(policy.getEffectiveTo())
                .status(policy.getStatus())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}

