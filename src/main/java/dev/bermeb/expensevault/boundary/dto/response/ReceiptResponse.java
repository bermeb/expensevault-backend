package dev.bermeb.expensevault.boundary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {

    private UUID id;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String description;
    private LocalDate date;
    private CategoryResponse category;
    private OcrDataResponse ocrData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}