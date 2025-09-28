package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.BonusPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BonusPolicyRepository extends JpaRepository<BonusPolicy, Long> {

    List<BonusPolicy> findByStatus(BonusPolicy.Status status);

    Optional<BonusPolicy> findByName(String name);

    @Query("SELECT bp FROM BonusPolicy bp WHERE bp.status = 'ACTIVE' AND " +
           "bp.effectiveFrom <= :date AND (bp.effectiveTo IS NULL OR bp.effectiveTo >= :date)")
    List<BonusPolicy> findActivePoliciesOnDate(@Param("date") LocalDate date);

    @Query("SELECT bp FROM BonusPolicy bp WHERE bp.type = :type AND bp.status = 'ACTIVE' AND " +
           "bp.effectiveFrom <= :date AND (bp.effectiveTo IS NULL OR bp.effectiveTo >= :date)")
    List<BonusPolicy> findActivePoliciesByTypeOnDate(@Param("type") BonusPolicy.BonusType type, 
                                                     @Param("date") LocalDate date);

    @Query("SELECT bp FROM BonusPolicy bp WHERE bp.type = 'CLIENT_COUNT' AND bp.status = 'ACTIVE' AND " +
           "bp.effectiveFrom <= :date AND (bp.effectiveTo IS NULL OR bp.effectiveTo >= :date) AND " +
           "bp.thresholdClients <= :clientCount " +
           "ORDER BY bp.thresholdClients DESC")
    List<BonusPolicy> findEligibleClientCountPolicies(@Param("date") LocalDate date, 
                                                      @Param("clientCount") Integer clientCount);

    @Query("SELECT bp FROM BonusPolicy bp WHERE bp.type = 'REVENUE_THRESHOLD' AND bp.status = 'ACTIVE' AND " +
           "bp.effectiveFrom <= :date AND (bp.effectiveTo IS NULL OR bp.effectiveTo >= :date) AND " +
           "bp.revenueThreshold <= :revenue " +
           "ORDER BY bp.revenueThreshold DESC")
    List<BonusPolicy> findEligibleRevenuePolicies(@Param("date") LocalDate date, 
                                                  @Param("revenue") java.math.BigDecimal revenue);

    @Query("SELECT COUNT(bp) > 0 FROM BonusPolicy bp WHERE bp.name = :name AND bp.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT COUNT(bp) > 0 FROM BonusPolicy bp WHERE bp.name = :name")
    boolean existsByName(@Param("name") String name);
}

