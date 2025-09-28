package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.ClientRequest;
import br.com.ecomercial.partnership.dto.ClientResponse;
import br.com.ecomercial.partnership.dto.ClientStatusChangeRequest;
import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.entity.Partner;
import br.com.ecomercial.partnership.entity.User;
import br.com.ecomercial.partnership.repository.ClientRepository;
import br.com.ecomercial.partnership.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final PartnerRepository partnerRepository;
    
    public ClientResponse createClient(String partnerId, ClientRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this partner
        if (currentUser.getRole() == User.Role.PARTNER && 
            !partnerId.equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        // Check if partner exists and is active
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        if (partner.getStatus() == Partner.Status.INACTIVE) {
            throw new RuntimeException("Partner inactive");
        }
        
        // Check if document is unique
        if (clientRepository.existsByDocument(request.getDocument())) {
            throw new RuntimeException("Document already exists");
        }
        
        Client client = Client.builder()
                .name(request.getName())
                .document(request.getDocument())
                .email(request.getEmail())
                .phone(request.getPhone())
                .clientType(request.getClientType())
                .monthlyBilling(request.getMonthlyBilling())
                .status(Client.Status.ACTIVE)
                .partnerId(partnerId)
                .build();
        
        Client savedClient = clientRepository.save(client);
        return mapToResponse(savedClient);
    }
    
    @Transactional(readOnly = true)
    public List<ClientResponse> getClientsByPartner(String partnerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this partner
        if (currentUser.getRole() == User.Role.PARTNER && 
            !partnerId.equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        return clientRepository.findByPartnerId(partnerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this client
        if (currentUser.getRole() == User.Role.PARTNER && 
            !client.getPartnerId().equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        return mapToResponse(client);
    }
    
    public ClientResponse updateClient(Long clientId, ClientRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this client
        if (currentUser.getRole() == User.Role.PARTNER && 
            !client.getPartnerId().equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        // Check if document is unique (excluding current client)
        if (!client.getDocument().equals(request.getDocument()) && 
            clientRepository.existsByDocument(request.getDocument())) {
            throw new RuntimeException("Document already exists");
        }
        
        client.setName(request.getName());
        client.setDocument(request.getDocument());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setClientType(request.getClientType());
        client.setMonthlyBilling(request.getMonthlyBilling());
        
        Client updatedClient = clientRepository.save(client);
        return mapToResponse(updatedClient);
    }
    
    public void deactivateClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this client
        if (currentUser.getRole() == User.Role.PARTNER && 
            !client.getPartnerId().equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        client.setStatus(Client.Status.INACTIVE);
        clientRepository.save(client);
    }
    
    public ClientResponse changeClientStatus(Long clientId, ClientStatusChangeRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        
        // Check if user has access to this client
        if (currentUser.getRole() == User.Role.PARTNER && 
            !client.getPartnerId().equals(currentUser.getPartnerId())) {
            throw new RuntimeException("Access denied");
        }
        
        // Validar se a data de vigência é no futuro
        if (request.getEffectiveFrom().isBefore(LocalDate.now())) {
            throw new RuntimeException("Effective from date must be in the future");
        }
        
        // Se a data de vigência é hoje, atualizar imediatamente
        if (request.getEffectiveFrom().equals(LocalDate.now())) {
            client.setStatus(request.getStatus());
            client.setStatusEffectiveFrom(request.getEffectiveFrom());
        } else {
            // Agendar para o futuro
            client.setStatusEffectiveFrom(request.getEffectiveFrom());
        }
        
        Client updatedClient = clientRepository.save(client);
        return mapToResponse(updatedClient);
    }
    
    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .document(client.getDocument())
                .email(client.getEmail())
                .phone(client.getPhone())
                .clientType(client.getClientType())
                .monthlyBilling(client.getMonthlyBilling())
                .status(client.getStatus())
                .partnerId(client.getPartnerId())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
