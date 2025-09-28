package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.ClosingDetail;
import br.com.ecomercial.partnership.entity.MonthlyClosing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Repository
public interface ClosingDetailRepository extends JpaRepository<ClosingDetail, Long> {

    List<ClosingDetail> findByMonthlyClosing(MonthlyClosing monthlyClosing);

    List<ClosingDetail> findByPartnerId(String partnerId);

    List<ClosingDetail> findByPartnerIdAndMonthlyClosing(String partnerId, MonthlyClosing monthlyClosing);

    @Query("SELECT cd FROM ClosingDetail cd WHERE cd.monthlyClosing.referenceMonth = :referenceMonth AND cd.partnerId = :partnerId")
    List<ClosingDetail> findByPartnerIdAndReferenceMonth(@Param("partnerId") String partnerId, 
                                                        @Param("referenceMonth") YearMonth referenceMonth);

    @Query("SELECT cd FROM ClosingDetail cd WHERE cd.monthlyClosing.referenceMonth = :referenceMonth ORDER BY cd.partnerId, cd.clientId")
    List<ClosingDetail> findByReferenceMonth(@Param("referenceMonth") YearMonth referenceMonth);

    @Query("SELECT cd.partnerId, SUM(cd.totalValue) FROM ClosingDetail cd WHERE cd.monthlyClosing.referenceMonth = :referenceMonth GROUP BY cd.partnerId")
    List<Object[]> getPartnerTotalsByReferenceMonth(@Param("referenceMonth") YearMonth referenceMonth);

    @Query("SELECT SUM(cd.totalValue) FROM ClosingDetail cd WHERE cd.monthlyClosing.referenceMonth = :referenceMonth AND cd.partnerId = :partnerId")
    BigDecimal getTotalValueByPartnerAndReferenceMonth(@Param("partnerId") String partnerId, 
                                                      @Param("referenceMonth") YearMonth referenceMonth);

    @Query("SELECT COUNT(DISTINCT cd.partnerId) FROM ClosingDetail cd WHERE cd.monthlyClosing.referenceMonth = :referenceMonth")
    Long countDistinctPartnersByReferenceMonth(@Param("referenceMonth") YearMonth referenceMonth);

    @Query("SELECT COUNT(cd) FROM ClosingDetail cd WHERE cd.monthlyClosing.referenceMonth = :referenceMonth")
    Long countClientsByReferenceMonth(@Param("referenceMonth") YearMonth referenceMonth);

    @Query("SELECT SUM(cd.commissionValue) FROM ClosingDetail cd WHERE cd.monthlyClosing.referenceMonth = :referenceMonth")
    BigDecimal getTotalCommissionByReferenceMonth(@Param("referenceMonth") YearMonth referenceMonth);

    @Query("SELECT SUM(cd.bonusValue) FROM ClosingDetail cd WHERE cd.monthlyClosing.referenceMonth = :referenceMonth")
    BigDecimal getTotalBonusByReferenceMonth(@Param("referenceMonth") YearMonth referenceMonth);

    @Query("SELECT COUNT(cd) FROM ClosingDetail cd WHERE cd.partnerId = :partnerId AND cd.monthlyClosing = :monthlyClosing")
    Long countByPartnerIdAndMonthlyClosing(@Param("partnerId") String partnerId, 
                                          @Param("monthlyClosing") MonthlyClosing monthlyClosing);
}

