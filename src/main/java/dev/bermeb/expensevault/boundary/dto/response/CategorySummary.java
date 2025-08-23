package dev.bermeb.expensevault.boundary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummary {
    private CategoryResponse category;
    private BigDecimal amount;
    private Float percentage;
    private Integer receiptCount;
}
