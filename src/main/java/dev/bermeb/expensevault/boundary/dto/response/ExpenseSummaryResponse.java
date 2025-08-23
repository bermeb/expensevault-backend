package dev.bermeb.expensevault.boundary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryResponse {
    private BigDecimal totalAmount;
    private String currency;
    private Integer receiptCount;
    private PeriodInfo period;
    private List<CategorySummary> categoryBreakdown;
    private BigDecimal averagePerDay;
    private List<MerchantSummary> topMerchants;
}