package dev.bermeb.expensevault.entity.repository;

import dev.bermeb.expensevault.entity.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, UUID>, JpaSpecificationExecutor<Receipt> {

    List<Receipt> findByDateBetweenOrderByDateDesc(LocalDate dateFrom, LocalDate dateTo);

    List<Receipt> findByCategoryNameOrderByDateDesc(String categoryName);

    List<Receipt> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Receipt r WHERE r.amount >= :minAmount ORDER BY r.date DESC")
    List<Receipt> findByMinAmount(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.date >= :startDate")
    Long countReceiptsFromDate(@Param("startDate") LocalDate startDate);

    @Query("SELECT SUM(r.amount) FROM Receipt r WHERE r.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT r.merchantName, SUM(r.amount), COUNT(r) FROM Receipt r " +
            "WHERE r.date BETWEEN :startDate AND :endDate " +
            "GROUP BY r.merchantName ORDER BY SUM(r.amount) DESC")
    List<Object[]> findTopMerchantsByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

}
