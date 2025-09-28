package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {
    
    Optional<Partner> findByDocument(String document);
    
    boolean existsByDocument(String document);
    
    List<Partner> findByStatus(Partner.Status status);
    
    List<Partner> findByNameContainingIgnoreCase(String name);
    
    Optional<Partner> findByPartnerId(String partnerId);
    
    boolean existsByPartnerId(String partnerId);
    
    Optional<Partner> findByEmail(String email);
}
