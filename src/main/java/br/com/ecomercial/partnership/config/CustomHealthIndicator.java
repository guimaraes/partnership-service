package br.com.ecomercial.partnership.config;

import br.com.ecomercial.partnership.repository.PartnerRepository;
import br.com.ecomercial.partnership.repository.ClientRepository;
import br.com.ecomercial.partnership.repository.MonthlyClosingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomHealthIndicator {

    private final PartnerRepository partnerRepository;
    private final ClientRepository clientRepository;
    private final MonthlyClosingRepository monthlyClosingRepository;

    public Map<String, Object> getHealthStatus() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // Verificar conectividade com banco de dados
            long partnerCount = partnerRepository.count();
            long clientCount = clientRepository.count();
            long closingCount = monthlyClosingRepository.count();
            
            details.put("status", "UP");
            details.put("database", "UP");
            details.put("partners_count", partnerCount);
            details.put("clients_count", clientCount);
            details.put("closings_count", closingCount);
            details.put("timestamp", LocalDateTime.now());
            
            // Verificar se há fechamentos recentes
            YearMonth currentMonth = YearMonth.now();
            boolean hasCurrentMonthClosing = monthlyClosingRepository.existsByReferenceMonth(currentMonth);
            details.put("current_month_closing", hasCurrentMonthClosing ? "EXISTS" : "MISSING");
            
            // Verificar se há parceiros ativos
            long activePartners = partnerRepository.count();
            details.put("active_partners", activePartners);
            
            // Verificar se há clientes ativos
            long activeClients = clientRepository.count();
            details.put("active_clients", activeClients);
            
            // Adicionar warnings se necessário
            if (activePartners == 0) {
                details.put("warning", "No active partners found");
            }
            
            if (activeClients == 0) {
                details.put("warning", "No active clients found");
            }
            
            if (!hasCurrentMonthClosing) {
                details.put("info", "No closing for current month yet");
            }
            
            return details;
                    
        } catch (Exception e) {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", "DOWN");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("timestamp", LocalDateTime.now());
            return errorDetails;
        }
    }
}
