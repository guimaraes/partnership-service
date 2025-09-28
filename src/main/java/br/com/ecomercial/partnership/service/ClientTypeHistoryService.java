package br.com.ecomercial.partnership.service;

import br.com.ecomercial.partnership.dto.ClientTypeChangeRequest;
import br.com.ecomercial.partnership.dto.ClientTypeHistoryResponse;
import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.entity.ClientTypeHistory;
import br.com.ecomercial.partnership.repository.ClientRepository;
import br.com.ecomercial.partnership.repository.ClientTypeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientTypeHistoryService {
    
    private final ClientTypeHistoryRepository clientTypeHistoryRepository;
    private final ClientRepository clientRepository;
    
    @Transactional(readOnly = true)
    public List<ClientTypeHistoryResponse> getClientTypeHistory(Long clientId) {
        return clientTypeHistoryRepository.findByClientIdOrderByEffectiveFromDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ClientTypeHistoryResponse getActiveTypeForDate(Long clientId, LocalDate date) {
        ClientTypeHistory history = clientTypeHistoryRepository.findActiveTypeForDate(clientId, date)
                .orElseThrow(() -> new RuntimeException("No active type found for client " + clientId + " on date " + date));
        return mapToResponse(history);
    }
    
    public ClientTypeHistoryResponse changeClientType(Long clientId, ClientTypeChangeRequest request) {
        // Validar se o cliente existe
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));
        
        // Validar se a data de vigência é no futuro
        if (request.getEffectiveFrom().isBefore(LocalDate.now())) {
            throw new RuntimeException("Effective from date must be in the future");
        }
        
        // Verificar se já existe uma alteração para esta data
        List<ClientTypeHistory> futureTypes = clientTypeHistoryRepository.findFutureTypesForClient(clientId, LocalDate.now());
        for (ClientTypeHistory existing : futureTypes) {
            if (existing.getEffectiveFrom().equals(request.getEffectiveFrom())) {
                throw new RuntimeException("A type change is already scheduled for this date");
            }
        }
        
        // Finalizar o histórico atual se existir
        finalizeCurrentHistory(clientId, request.getEffectiveFrom().minusDays(1));
        
        // Criar novo histórico
        ClientTypeHistory newHistory = ClientTypeHistory.builder()
                .clientId(clientId)
                .clientType(request.getClientType())
                .effectiveFrom(request.getEffectiveFrom())
                .build();
        
        ClientTypeHistory savedHistory = clientTypeHistoryRepository.save(newHistory);
        
        // Se a data de vigência é hoje, atualizar o cliente imediatamente
        if (request.getEffectiveFrom().equals(LocalDate.now())) {
            client.setClientType(request.getClientType());
            clientRepository.save(client);
        }
        
        return mapToResponse(savedHistory);
    }
    
    private void finalizeCurrentHistory(Long clientId, LocalDate endDate) {
        // Buscar o histórico ativo atual
        ClientTypeHistory activeHistory = clientTypeHistoryRepository.findActiveTypeForDate(clientId, LocalDate.now())
                .orElse(null);
        
        if (activeHistory != null && activeHistory.getEffectiveTo() == null) {
            activeHistory.setEffectiveTo(endDate);
            clientTypeHistoryRepository.save(activeHistory);
        }
    }
    
    private ClientTypeHistoryResponse mapToResponse(ClientTypeHistory history) {
        return ClientTypeHistoryResponse.builder()
                .id(history.getId())
                .clientId(history.getClientId())
                .clientType(history.getClientType())
                .effectiveFrom(history.getEffectiveFrom())
                .effectiveTo(history.getEffectiveTo())
                .createdAt(history.getCreatedAt())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}

