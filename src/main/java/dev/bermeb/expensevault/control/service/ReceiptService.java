package dev.bermeb.expensevault.control.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.bermeb.expensevault.boundary.dto.request.ReceiptUpdateRequest;
import dev.bermeb.expensevault.boundary.dto.response.OcrResult;
import dev.bermeb.expensevault.control.exception.CategoryNotFoundException;
import dev.bermeb.expensevault.entity.model.Category;
import dev.bermeb.expensevault.entity.model.OcrData;
import dev.bermeb.expensevault.entity.model.Receipt;
import dev.bermeb.expensevault.entity.repository.CategoryRepository;
import dev.bermeb.expensevault.entity.repository.OcrDataRepository;
import dev.bermeb.expensevault.entity.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final CategoryRepository categoryRepository;
    private final OcrDataRepository ocrDataRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<Receipt> findAll(String categoryName, LocalDate startDate, LocalDate endDate, Sort sort, int limit) {
        Specification<Receipt> spec = null;

        if (categoryName != null && !categoryName.isEmpty()) {
            spec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category").get("name"), categoryName);
        }

        if (startDate != null) {
            Specification<Receipt> startDateSpec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate);
            spec = spec == null ? startDateSpec : spec.and(startDateSpec);
        }

        if (endDate != null) {
            Specification<Receipt> endDateSpec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate);
            spec = spec == null ? endDateSpec : spec.and(endDateSpec);
        }

        if (spec == null) {
            return receiptRepository.findAll(sort).stream().limit(limit).toList();
        }

        return receiptRepository.findAll(spec, sort).stream().limit(limit).toList();
    }

    public Receipt save(Receipt receipt, OcrResult ocrResult, int processingTime) {
        Receipt savedReceipt = receiptRepository.save(receipt);

        // Save OCR data
        OcrData ocrData = OcrData.builder()
                .receipt(savedReceipt)
                .rawText(ocrResult.getRawText())
                .confidence(ocrResult.getConfidence())
                .processedAt(LocalDateTime.now())
                .processingTimeMs(processingTime)
                .extractedFields(convertToJson(ocrResult))
                .build();

        ocrDataRepository.save(ocrData);
        savedReceipt.setOcrData(ocrData);

        log.info("Receipt saved with ID: {}", savedReceipt.getId());
        return savedReceipt;
    }

    public Receipt update(Receipt receipt, ReceiptUpdateRequest request) {
        if (request.getAmount() != null) {
            receipt.setAmount(request.getAmount());
        }
        if (request.getMerchantName() != null) {
            receipt.setMerchantName(request.getMerchantName());
        }
        if (request.getDescription() != null) {
            receipt.setDescription(request.getDescription());
        }
        if (request.getDate() != null) {
            receipt.setDate(request.getDate());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
            receipt.setCategory(category);
        }

        Receipt updatedReceipt = receiptRepository.save(receipt);
        log.info("Receipt updated with ID: {}", updatedReceipt.getId());
        return updatedReceipt;
    }

    public Receipt updateWithNewOcr(Receipt receipt, OcrResult newOcrResult) {
        Receipt updatedReceipt = receiptRepository.save(receipt);

        // Update OCR data
        OcrData existingOcrData = receipt.getOcrData();
        if (existingOcrData != null) {
            existingOcrData.setRawText(newOcrResult.getRawText());
            existingOcrData.setConfidence(newOcrResult.getConfidence());
            existingOcrData.setProcessedAt(LocalDateTime.now());
            existingOcrData.setExtractedFields(convertToJson(newOcrResult));
            ocrDataRepository.save(existingOcrData);
        }

        log.info("Receipt reprocessed with ID: {}", updatedReceipt.getId());
        return updatedReceipt;
    }

    @Transactional(readOnly = true)
    public Optional<Receipt> findById(UUID id) {
        return receiptRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return receiptRepository.existsById(id);
    }

    public void deleteById(UUID id) {
        receiptRepository.deleteById(id);
        log.info("Receipt deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Category findCategoryByName(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryName));
    }

    private String convertToJson(OcrResult ocrResult) {
        try {
            Map<String, Object> data = Map.of(
                    "detectedAmount", ocrResult.getTotalAmount() != null ? ocrResult.getTotalAmount() : 0,
                    "detectedDate", ocrResult.getDate() != null ? ocrResult.getDate().toString() : "",
                    "detectedMerchant", ocrResult.getMerchantName() != null ? ocrResult.getMerchantName() : "",
                    "detectedItems", ocrResult.getDetectedItems() != null ? ocrResult.getDetectedItems() : List.of()
            );
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to serialize OCR result to JSON", e);
            return "{}";
        }
    }
}