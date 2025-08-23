package dev.bermeb.expensevault.boundary.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @Size(max = 255)
    private String merchantName;

    @Size(max = 1000)
    private String description;

    private LocalDate date;

    private UUID categoryId;
}