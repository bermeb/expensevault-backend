package dev.bermeb.expensevault.control.service;

import dev.bermeb.expensevault.boundary.dto.response.ExpenseSummaryResponse;
import dev.bermeb.expensevault.boundary.dto.response.MerchantSummary;
import dev.bermeb.expensevault.boundary.dto.response.PeriodInfo;
import dev.bermeb.expensevault.entity.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final ReceiptRepository receiptRepository;

    // TODO: Add category filtering and other report types?
    public ExpenseSummaryResponse generateSummary(LocalDate startDate, LocalDate endDate) {
        log.info("Generating expense summary for period: {} to {}", startDate, endDate);

        BigDecimal totalAmount = receiptRepository.sumAmountByDateRange(startDate, endDate);
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;

        Long receiptCount = receiptRepository.countReceiptsFromDate(startDate);
        if (receiptCount == null) receiptCount = 0L;

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal averagePerDay = daysBetween > 0 ?
                totalAmount.divide(BigDecimal.valueOf(daysBetween), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Get top merchants
        List<Object[]> merchantData = receiptRepository.findTopMerchantsByDateRange(startDate, endDate);
        List<MerchantSummary> topMerchants = merchantData.stream()
                .map(data -> MerchantSummary.builder()
                        .merchantName((String) data[0])
                        .amount((BigDecimal) data[1])
                        .receiptCount(((Number) data[2]).intValue())
                        .build())
                .collect(Collectors.toList());

        PeriodInfo periodInfo = PeriodInfo.builder()
                .startDate(startDate)
                .endDate(endDate)
                .type("CUSTOM")
                .build();

        return ExpenseSummaryResponse.builder()
                .totalAmount(totalAmount)
                .currency("EUR") // TODO: Make dynamic based on user settings
                .receiptCount(receiptCount.intValue())
                .period(periodInfo)
                .averagePerDay(averagePerDay)
                .topMerchants(topMerchants)
                .build();
    }
}
