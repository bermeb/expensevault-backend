package dev.bermeb.expensevault.boundary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Result object containing extracted OCR data
 * No API exposure, internal use only
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    private String rawText;
    private BigDecimal totalAmount;
    private LocalDate date;
    private String merchantName;
    private Float confidence;
    private List<String> detectedItems;
}
