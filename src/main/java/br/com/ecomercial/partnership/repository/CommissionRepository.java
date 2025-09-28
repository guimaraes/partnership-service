package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    
    List<Commission> findByPartnerId(String partnerId);
    
    List<Commission> findByPartnerIdAndReferenceMonth(String partnerId, YearMonth referenceMonth);
    
    List<Commission> findByClientId(Long clientId);
    
    List<Commission> findByStatus(Commission.Status status);
    
    Optional<Commission> findByPartnerIdAndClientIdAndReferenceMonth(String partnerId, Long clientId, YearMonth referenceMonth);
    
    List<Commission> findByReferenceMonth(YearMonth referenceMonth);
}

