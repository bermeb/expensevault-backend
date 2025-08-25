package dev.bermeb.expensevault.boundary.controller;

import dev.bermeb.expensevault.boundary.dto.response.OcrExtractionResponse;
import dev.bermeb.expensevault.boundary.dto.response.OcrResult;
import dev.bermeb.expensevault.control.exception.InvalidFileException;
import dev.bermeb.expensevault.control.exception.OrcProcessingException;
import dev.bermeb.expensevault.control.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OCR", description = "Endpoints for testing the OCR processing")
public class OcrController {

    private final OcrService ocrService;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Extract text from an image using OCR", description = "Uploads an image and extracts text using OCR technology.")
    public ResponseEntity<OcrExtractionResponse> extractText(@RequestParam("file") MultipartFile file) {
        long startTime = System.nanoTime();

        try {
            if (file.isEmpty()) {
                throw new InvalidFileException("File is empty");
            }

            log.info("Processing OCR extraction for file: {}", file.getOriginalFilename());

            OcrResult result = ocrService.extractReceiptData(file.getBytes());

            int processingTimeMs = (int) ((System.nanoTime() - startTime) / 1_000_000);

            OcrExtractionResponse response = OcrExtractionResponse.builder()
                    .rawText(result.getRawText())
                    .confidence(result.getConfidence())
                    .extractedData(Map.of(
                            "amount", result.getTotalAmount() != null ? result.getTotalAmount() : 0,
                            "date", result.getDate() != null ? result.getDate().toString() : "N/A",
                            "merchant", result.getMerchantName() != null ? result.getMerchantName() : "N/A",
                            "items", result.getDetectedItems() != null ? result.getDetectedItems() : List.of()
                    ))
                    .processingTimeMs(processingTimeMs)
                    .build();

            log.info("OCR extraction completed in {}ms with {} confidence for file: {}", processingTimeMs, result.getConfidence(), file.getOriginalFilename());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OCR extraction failed for file: {}", file.getOriginalFilename(), e);
            throw new OrcProcessingException("OCR extraction failed, e");
        }
    }
}