package dev.bermeb.expensevault.entity.repository;

import dev.bermeb.expensevault.entity.model.OcrData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OcrDataRepository extends JpaRepository<OcrData, UUID> {

    Optional<OcrData> findByReceiptId(UUID receiptId);

    @Query("SELECT AVG(o.confidence) FROM OcrData o WHERE o.confidence IS NOT NULL")
    Double findAverageConfidence();

    @Query("SELECT o FROM OcrData o WHERE o.confidence < :minConfidence")
    List<OcrData> findLowConfidenceOcrData(@Param("minConfidence") Float minConfidence);

    @Query("SELECT AVG(o.processingTimeMs) FROM OcrData o WHERE o.processingTimeMs IS NOT NULL")
    Double findAverageProcessingTime();

}
