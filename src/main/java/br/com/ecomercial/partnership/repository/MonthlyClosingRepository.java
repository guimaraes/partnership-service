package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.MonthlyClosing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyClosingRepository extends JpaRepository<MonthlyClosing, Long> {

    Optional<MonthlyClosing> findByReferenceMonth(YearMonth referenceMonth);

    boolean existsByReferenceMonth(YearMonth referenceMonth);

    List<MonthlyClosing> findByStatus(MonthlyClosing.Status status);

    List<MonthlyClosing> findByStatusOrderByReferenceMonthDesc(MonthlyClosing.Status status);

    @Query("SELECT mc FROM MonthlyClosing mc ORDER BY mc.referenceMonth DESC")
    List<MonthlyClosing> findAllOrderByReferenceMonthDesc();

    @Query("SELECT COUNT(mc) FROM MonthlyClosing mc WHERE mc.status = :status")
    Long countByStatus(@Param("status") MonthlyClosing.Status status);

    @Query("SELECT mc FROM MonthlyClosing mc WHERE mc.referenceMonth >= :startMonth AND mc.referenceMonth <= :endMonth ORDER BY mc.referenceMonth DESC")
    List<MonthlyClosing> findByReferenceMonthBetween(@Param("startMonth") YearMonth startMonth, 
                                                    @Param("endMonth") YearMonth endMonth);

    @Query("SELECT mc FROM MonthlyClosing mc WHERE mc.status = 'COMPLETED' AND mc.referenceMonth >= :startMonth ORDER BY mc.referenceMonth DESC")
    List<MonthlyClosing> findCompletedClosingsFromMonth(@Param("startMonth") YearMonth startMonth);

    @Query("SELECT MAX(mc.referenceMonth) FROM MonthlyClosing mc WHERE mc.status = 'COMPLETED'")
    Optional<YearMonth> findLastCompletedMonth();
}

