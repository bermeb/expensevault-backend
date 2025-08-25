package dev.bermeb.expensevault.boundary.controller;

import dev.bermeb.expensevault.boundary.dto.request.ReceiptUpdateRequest;
import dev.bermeb.expensevault.boundary.dto.response.OcrResult;
import dev.bermeb.expensevault.boundary.dto.response.ReceiptResponse;
import dev.bermeb.expensevault.boundary.mapper.ReceiptMapper;
import dev.bermeb.expensevault.control.exception.InvalidFileException;
import dev.bermeb.expensevault.control.exception.ReceiptNotFoundException;
import dev.bermeb.expensevault.control.exception.ReceiptProcessingException;
import dev.bermeb.expensevault.control.service.OcrService;
import dev.bermeb.expensevault.control.service.ReceiptService;
import dev.bermeb.expensevault.entity.model.Receipt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/receipts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Receipts", description = "Endpoints for managing receipts")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final OcrService ocrService;
    private final ReceiptMapper receiptMapper;

    // TODO: Change limit parameter to be configurable via request param or be unlimited
    // TODO: Change sort parameter to be configurable via request param
    @GetMapping
    @Operation(summary = "Get All Receipts", description = "Retrieve a list of all receipts.")
    public ResponseEntity<List<ReceiptResponse>> getAllReceipts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate) {

        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "date");

        List<Receipt> receipts = receiptService.findAll(category, startDate, endDate, sort, 1000);
        List<ReceiptResponse> responses = receipts.stream()
                .map(receiptMapper::toResponse)
                .collect(Collectors.toList());

        log.info("Received {} receipts with filters - category: {}, dateFrom: {}, dateTo: {}", responses.size(), category, startDate, endDate);

        return ResponseEntity.ok(responses);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Receipt", description = "Upload a new receipt image for OCR processing and storage.")
    public ResponseEntity<ReceiptResponse> uploadReceipt(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description) {

        long startTime = System.nanoTime();

        try {
            validateFile(file);

            log.info("Processing receipt upload - filename: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

            OcrResult ocrResult = ocrService.extractReceiptData(file.getBytes());

            Receipt receipt = Receipt.builder()
                    .amount(ocrResult.getTotalAmount())
                    .merchantName(ocrResult.getMerchantName())
                    .date(ocrResult.getDate() != null ? ocrResult.getDate() : LocalDate.now())
                    .description(description)
                    .currency("EUR") // TODO: Extract currency from OCR result
                    .build();

            if (category != null) {
                receipt.setCategory(receiptService.findCategoryByName(category));
            }

            int processingTimeMs = (int) ((System.nanoTime() - startTime) / 1_000_000);
            Receipt savedReceipt = receiptService.save(receipt, ocrResult, processingTimeMs);

            log.info("Receipt processed and saved with ID: {} in {}ms", savedReceipt.getId(), processingTimeMs);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(receiptMapper.toResponse(savedReceipt));
        } catch (Exception e) {
            log.error("Receipt processing failed for file: {}", file.getOriginalFilename(), e);
            throw new ReceiptProcessingException("Receipt processing failed", e);
        }
    }

    @GetMapping("/{receiptId}")
    @Operation(summary = "Get Receipt by ID", description = "Retrieve a specific receipt by its ID.")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable UUID receiptId) {
        Receipt receipt = receiptService.findById(receiptId)
                .orElseThrow(() -> new ReceiptNotFoundException(receiptId));

        return ResponseEntity.ok(receiptMapper.toResponse(receipt));
    }

    @PutMapping("/{receiptId}")
    @Operation(summary = "Update Receipt", description = "Update an existing receipt's details.")
    public ResponseEntity<ReceiptResponse> updateReceipt(
            @PathVariable UUID receiptId,
            @Valid @RequestBody ReceiptUpdateRequest request) {

        Receipt receipt = receiptService.findById(receiptId)
                .orElseThrow(() -> new ReceiptNotFoundException(receiptId));

        Receipt updatedReceipt = receiptService.update(receipt, request);

        log.info("Updated Receipt with ID: {}", updatedReceipt.getId());

        return ResponseEntity.ok(receiptMapper.toResponse(updatedReceipt));
    }

    @DeleteMapping("/{receiptId}")
    @Operation(summary = "Delete Receipt", description = "Delete a receipt by its ID.")
    public ResponseEntity<Void> deleteReceipt(@PathVariable UUID receiptId) {
        if (!receiptService.existsById(receiptId)) {
            throw new ReceiptNotFoundException(receiptId);
        }

        receiptService.deleteById(receiptId);

        log.info("Deleted Receipt with ID: {}", receiptId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{receiptId}/reprocess")
    @Operation(summary = "Reprocess Receipt OCR", description = "Reprocess the OCR data for an existing receipt.")
    public ResponseEntity<ReceiptResponse> reprocessReceipt(
            @PathVariable UUID receiptId,
            @RequestParam("file") MultipartFile file) {

        try {
            validateFile(file);

            Receipt receipt = receiptService.findById(receiptId)
                    .orElseThrow(() -> new ReceiptNotFoundException(receiptId));

            log.info("Reprocessing receipt with ID: {}", receiptId);

            OcrResult newOcrResult = ocrService.extractReceiptData(file.getBytes());

            receipt.setAmount(newOcrResult.getTotalAmount());
            receipt.setMerchantName(newOcrResult.getMerchantName());
            if (newOcrResult.getDate() != null) {
                receipt.setDate(newOcrResult.getDate());
            }

            Receipt updatedReceipt = receiptService.updateWithNewOcr(receipt, newOcrResult);

            log.info("Reprocessed Receipt with ID: {}", updatedReceipt.getId());

            return ResponseEntity.ok(receiptMapper.toResponse(updatedReceipt));
        } catch (Exception e) {
            log.error("Receipt reprocessing failed for ID: {}", receiptId, e);
            throw new ReceiptProcessingException("Receipt reprocessing failed", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new InvalidFileException("Unsupported file type: " + contentType + ". Supported types are: PNG, JPG and PDF");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10 MB limit
            throw new InvalidFileException("File size exceeds the maximum limit of 10MB");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("application/pdf");
    }
}