package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.PartnerRequest;
import br.com.ecomercial.partnership.dto.PartnerResponse;
import br.com.ecomercial.partnership.entity.Partner;
import br.com.ecomercial.partnership.entity.User;
import br.com.ecomercial.partnership.repository.PartnerRepository;
import br.com.ecomercial.partnership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PartnerService {
    
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final MetricsService metricsService;
    
    public PartnerResponse createPartner(PartnerRequest request) {
        if (partnerRepository.existsByDocument(request.getDocument())) {
            throw new RuntimeException("Document already exists");
        }
        
        String partnerId = "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Partner partner = Partner.builder()
                .partnerId(partnerId)
                .name(request.getName())
                .document(request.getDocument())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(Partner.Status.ACTIVE)
                .build();
        
        Partner savedPartner = partnerRepository.save(partner);
        
        // Incrementar métrica de criação de parceiro
        metricsService.incrementPartnerCreated();
        
        return mapToResponse(savedPartner);
    }
    
    @Transactional(readOnly = true)
    public List<PartnerResponse> getAllPartners() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        if (currentUser.getRole() == User.Role.ADMIN) {
            return partnerRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else {
            // Partner can only see their own data
            return partnerRepository.findByPartnerId(currentUser.getPartnerId())
                    .map(partner -> List.of(mapToResponse(partner)))
                    .orElse(List.of());
        }
    }
    
    @Transactional(readOnly = true)
    public PartnerResponse getPartnerById(String partnerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this partner
        if (currentUser.getRole() == User.Role.PARTNER && 
            !partnerId.equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        return mapToResponse(partner);
    }
    
    public PartnerResponse updatePartner(String partnerId, PartnerRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this partner
        if (currentUser.getRole() == User.Role.PARTNER && 
            !partnerId.equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        // Check if document is unique (excluding current partner)
        if (!partner.getDocument().equals(request.getDocument()) && 
            partnerRepository.existsByDocument(request.getDocument())) {
            throw new RuntimeException("Document already exists");
        }
        
        partner.setName(request.getName());
        partner.setDocument(request.getDocument());
        partner.setEmail(request.getEmail());
        partner.setPhone(request.getPhone());
        
        Partner updatedPartner = partnerRepository.save(partner);
        
        // Incrementar métrica de atualização de parceiro
        metricsService.incrementPartnerUpdated();
        
        return mapToResponse(updatedPartner);
    }
    
    public void deactivatePartner(String partnerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Only ADMIN can deactivate partners
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        partner.setStatus(Partner.Status.INACTIVE);
        partnerRepository.save(partner);
        
        // Incrementar métrica de exclusão de parceiro
        metricsService.incrementPartnerDeleted();
    }
    
    private PartnerResponse mapToResponse(Partner partner) {
        return PartnerResponse.builder()
                .partnerId(partner.getPartnerId())
                .name(partner.getName())
                .document(partner.getDocument())
                .email(partner.getEmail())
                .phone(partner.getPhone())
                .status(partner.getStatus())
                .createdAt(partner.getCreatedAt())
                .updatedAt(partner.getUpdatedAt())
                .build();
    }
}

