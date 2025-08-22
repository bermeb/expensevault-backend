package dev.bermeb.expensevault.entity.repository;

import dev.bermeb.expensevault.entity.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.receipts")
    List<Category> findAllWithReceipts();

    @Query("SELECT c, COUNT(r) as receiptCount FROM Category c " +
            "LEFT JOIN c.receipts r GROUP BY c ORDER BY COUNT(r) DESC")
    List<Object[]> findCategoriesWithReceiptCount();

}
