package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.CommissionRuleRequest;
import br.com.ecomercial.partnership.entity.Partner;
import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.entity.CommissionRule;
import br.com.ecomercial.partnership.exception.BusinessException;
import br.com.ecomercial.partnership.exception.ValidationException;
import br.com.ecomercial.partnership.repository.ClientRepository;
import br.com.ecomercial.partnership.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final PartnerRepository partnerRepository;
    private final ClientRepository clientRepository;

    /**
     * Valida campos obrigatórios de um parceiro
     */
    public void validatePartner(Partner partner) {
        Map<String, String> errors = new HashMap<>();

        if (!StringUtils.hasText(partner.getName())) {
            errors.put("name", "field name is required");
        }

        if (!StringUtils.hasText(partner.getDocument())) {
            errors.put("document", "field document is required");
        }

        if (!StringUtils.hasText(partner.getEmail())) {
            errors.put("email", "field email is required");
        }

        if (!StringUtils.hasText(partner.getPhone())) {
            errors.put("phone", "field phone is required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Valida campos obrigatórios de um cliente
     */
    public void validateClient(Client client) {
        Map<String, String> errors = new HashMap<>();

        if (!StringUtils.hasText(client.getName())) {
            errors.put("name", "field name is required");
        }

        if (!StringUtils.hasText(client.getEmail())) {
            errors.put("email", "field email is required");
        }

        if (!StringUtils.hasText(client.getPhone())) {
            errors.put("phone", "field phone is required");
        }

        if (!StringUtils.hasText(client.getDocument())) {
            errors.put("document", "field document is required");
        }

        if (!StringUtils.hasText(client.getPartnerId())) {
            errors.put("partnerId", "field partnerId is required");
        }

        if (client.getClientType() == null) {
            errors.put("type", "field type is required");
        }

        if (client.getMonthlyBilling() == null) {
            errors.put("monthlyBilling", "field monthlyBilling is required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Valida campos obrigatórios de uma regra de comissão
     */
    public void validateCommissionRule(CommissionRule rule) {
        Map<String, String> errors = new HashMap<>();

        if (rule.getClientType() == null) {
            errors.put("clientType", "field clientType is required");
        }

        if (rule.getEffectiveFrom() == null) {
            errors.put("effectiveFrom", "field effectiveFrom is required");
        }

        if (rule.getFixedCommission() == null) {
            errors.put("fixedCommission", "field fixedCommission is required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Valida campos obrigatórios de uma regra de comissão (DTO)
     */
    public void validateCommissionRule(CommissionRuleRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.getClientType() == null) {
            errors.put("clientType", "field clientType is required");
        }

        if (request.getEffectiveFrom() == null) {
            errors.put("effectiveFrom", "field effectiveFrom is required");
        }

        if (request.getFixedCommission() == null) {
            errors.put("fixedCommission", "field fixedCommission is required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Valida se um parceiro pode ser removido
     */
    public void validatePartnerDeletion(String partnerId) {
        long activeClientsCount = clientRepository.countByPartnerIdAndStatus(partnerId, Client.Status.ACTIVE);
        
        if (activeClientsCount > 0) {
            throw new BusinessException("partner has active clients", "PARTNER_HAS_ACTIVE_CLIENTS");
        }
    }

    /**
     * Valida se um documento já existe
     */
    public void validateUniqueDocument(String document, String currentPartnerId) {
        if (StringUtils.hasText(document)) {
            partnerRepository.findByDocument(document)
                    .ifPresent(partner -> {
                        if (!partner.getPartnerId().equals(currentPartnerId)) {
                            throw new ValidationException("document", "Document already exists");
                        }
                    });
        }
    }

    /**
     * Valida se um email já existe
     */
    public void validateUniqueEmail(String email, String currentPartnerId) {
        if (StringUtils.hasText(email)) {
            partnerRepository.findByEmail(email)
                    .ifPresent(partner -> {
                        if (!partner.getPartnerId().equals(currentPartnerId)) {
                            throw new ValidationException("email", "Email already exists");
                        }
                    });
        }
    }

    /**
     * Valida formato de email
     */
    public void validateEmailFormat(String email) {
        if (StringUtils.hasText(email) && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("email", "Invalid email format");
        }
    }

    /**
     * Valida formato de documento (CPF/CNPJ)
     */
    public void validateDocumentFormat(String document) {
        if (StringUtils.hasText(document)) {
            String cleanDocument = document.replaceAll("[^0-9]", "");
            
            if (cleanDocument.length() == 11) {
                // Validação básica de CPF
                if (!isValidCPF(cleanDocument)) {
                    throw new ValidationException("document", "Invalid CPF format");
                }
            } else if (cleanDocument.length() == 14) {
                // Validação básica de CNPJ
                if (!isValidCNPJ(cleanDocument)) {
                    throw new ValidationException("document", "Invalid CNPJ format");
                }
            } else {
                throw new ValidationException("document", "Document must be a valid CPF (11 digits) or CNPJ (14 digits)");
            }
        }
    }

    private boolean isValidCPF(String cpf) {
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) firstDigit = 0;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) secondDigit = 0;

        return Character.getNumericValue(cpf.charAt(9)) == firstDigit &&
               Character.getNumericValue(cpf.charAt(10)) == secondDigit;
    }

    private boolean isValidCNPJ(String cnpj) {
        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
        }
        int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
        }
        int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

        return Character.getNumericValue(cnpj.charAt(12)) == firstDigit &&
               Character.getNumericValue(cnpj.charAt(13)) == secondDigit;
    }
}
