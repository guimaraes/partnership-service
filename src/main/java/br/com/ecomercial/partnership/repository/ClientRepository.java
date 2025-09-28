package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByDocument(String document);
    
    boolean existsByDocument(String document);
    
    List<Client> findByPartnerId(String partnerId);
    
    List<Client> findByPartnerIdAndStatus(String partnerId, Client.Status status);
    
    List<Client> findByClientType(Client.ClientType clientType);
    
    List<Client> findByStatus(Client.Status status);
    
    long countByPartnerIdAndStatus(String partnerId, Client.Status status);
}

