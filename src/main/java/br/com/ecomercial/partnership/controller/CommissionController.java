package br.com.ecomercial.partnership.controller;

import br.com.ecomercial.partnership.dto.CommissionResponse;
import br.com.ecomercial.partnership.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/commissions")
@RequiredArgsConstructor
public class CommissionController {
    
    private final CommissionService commissionService;
    
    @PostMapping("/calculate/{year}/{month}")
    public ResponseEntity<Void> calculateCommissionsForMonth(
            @PathVariable int year, 
            @PathVariable int month) {
        YearMonth referenceMonth = YearMonth.of(year, month);
        commissionService.calculateCommissionsForMonth(referenceMonth);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/calculate-with-bonus/{year}/{month}")
    public ResponseEntity<Void> calculateCommissionsWithPerformanceBonusForMonth(
            @PathVariable int year, 
            @PathVariable int month) {
        YearMonth referenceMonth = YearMonth.of(year, month);
        commissionService.calculateCommissionsWithPerformanceBonusForMonth(referenceMonth);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<CommissionResponse>> getCommissionsByPartner(@PathVariable String partnerId) {
        List<CommissionResponse> commissions = commissionService.getCommissionsByPartner(partnerId);
        return ResponseEntity.ok(commissions);
    }
    
    @GetMapping("/partner/{partnerId}/{year}/{month}")
    public ResponseEntity<List<CommissionResponse>> getCommissionsByPartnerAndMonth(
            @PathVariable String partnerId,
            @PathVariable int year, 
            @PathVariable int month) {
        YearMonth referenceMonth = YearMonth.of(year, month);
        List<CommissionResponse> commissions = commissionService.getCommissionsByPartnerAndMonth(partnerId, referenceMonth);
        return ResponseEntity.ok(commissions);
    }
    
    @GetMapping
    public ResponseEntity<List<CommissionResponse>> getAllCommissions() {
        List<CommissionResponse> commissions = commissionService.getAllCommissions();
        return ResponseEntity.ok(commissions);
    }
    
    @PutMapping("/{commissionId}/approve")
    public ResponseEntity<Void> approveCommission(@PathVariable Long commissionId) {
        commissionService.approveCommission(commissionId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{commissionId}/pay")
    public ResponseEntity<Void> payCommission(@PathVariable Long commissionId) {
        commissionService.payCommission(commissionId);
        return ResponseEntity.ok().build();
    }
}
