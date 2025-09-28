package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.Client;
import br.com.ecomercial.partnership.entity.ClientTypeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientTypeHistoryRepository extends JpaRepository<ClientTypeHistory, Long> {
    
    List<ClientTypeHistory> findByClientIdOrderByEffectiveFromDesc(Long clientId);
    
    @Query("SELECT cth FROM ClientTypeHistory cth WHERE cth.clientId = :clientId AND " +
           "cth.effectiveFrom <= :date AND (cth.effectiveTo IS NULL OR cth.effectiveTo >= :date) " +
           "ORDER BY cth.effectiveFrom DESC")
    Optional<ClientTypeHistory> findActiveTypeForDate(@Param("clientId") Long clientId, @Param("date") LocalDate date);
    
    @Query("SELECT cth FROM ClientTypeHistory cth WHERE cth.clientId = :clientId AND " +
           "((cth.effectiveFrom <= :startDate AND (cth.effectiveTo IS NULL OR cth.effectiveTo >= :startDate)) OR " +
           "(cth.effectiveFrom <= :endDate AND (cth.effectiveTo IS NULL OR cth.effectiveTo >= :endDate)) OR " +
           "(cth.effectiveFrom >= :startDate AND cth.effectiveFrom <= :endDate)) " +
           "ORDER BY cth.effectiveFrom ASC")
    List<ClientTypeHistory> findByClientIdAndPeriod(@Param("clientId") Long clientId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT cth FROM ClientTypeHistory cth WHERE cth.clientId = :clientId AND " +
           "cth.effectiveFrom > :date " +
           "ORDER BY cth.effectiveFrom ASC")
    List<ClientTypeHistory> findFutureTypesForClient(@Param("clientId") Long clientId, @Param("date") LocalDate date);
}

