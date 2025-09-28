package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.entity.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {
    
    Optional<CommissionRule> findByClientType(Client.ClientType clientType);
    
    boolean existsByClientType(Client.ClientType clientType);
    
    List<CommissionRule> findByStatus(CommissionRule.Status status);
    
    Optional<CommissionRule> findByClientTypeAndStatus(Client.ClientType clientType, CommissionRule.Status status);
    
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.clientType = :clientType AND " +
           "cr.effectiveFrom <= :date AND (cr.effectiveTo IS NULL OR cr.effectiveTo >= :date) AND " +
           "cr.status = 'ACTIVE' " +
           "ORDER BY cr.effectiveFrom DESC")
    Optional<CommissionRule> findActiveRuleForClientTypeAndDate(@Param("clientType") Client.ClientType clientType, 
                                                               @Param("date") LocalDate date);
    
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.clientType = :clientType AND " +
           "((cr.effectiveFrom <= :startDate AND (cr.effectiveTo IS NULL OR cr.effectiveTo >= :startDate)) OR " +
           "(cr.effectiveFrom <= :endDate AND (cr.effectiveTo IS NULL OR cr.effectiveTo >= :endDate)) OR " +
           "(cr.effectiveFrom >= :startDate AND cr.effectiveFrom <= :endDate)) AND " +
           "cr.status = 'ACTIVE' " +
           "ORDER BY cr.effectiveFrom ASC")
    List<CommissionRule> findActiveRulesForClientTypeAndPeriod(@Param("clientType") Client.ClientType clientType,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.clientType = :clientType AND " +
           "cr.status = 'ACTIVE' " +
           "ORDER BY cr.effectiveFrom DESC")
    List<CommissionRule> findAllActiveRulesForClientType(@Param("clientType") Client.ClientType clientType);
    
    @Query("SELECT COUNT(cr) > 0 FROM CommissionRule cr WHERE cr.clientType = :clientType AND " +
           "cr.status = 'ACTIVE' AND " +
           "((cr.effectiveFrom <= :effectiveFrom AND (cr.effectiveTo IS NULL OR cr.effectiveTo >= :effectiveFrom)) OR " +
           "(cr.effectiveFrom <= :effectiveTo AND (cr.effectiveTo IS NULL OR cr.effectiveTo >= :effectiveTo)) OR " +
           "(cr.effectiveFrom >= :effectiveFrom AND cr.effectiveFrom <= :effectiveTo))")
    boolean existsOverlappingRule(@Param("clientType") Client.ClientType clientType,
                                 @Param("effectiveFrom") LocalDate effectiveFrom,
                                 @Param("effectiveTo") LocalDate effectiveTo);
}
