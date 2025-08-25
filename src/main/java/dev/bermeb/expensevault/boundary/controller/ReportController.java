package dev.bermeb.expensevault.boundary.controller;

import dev.bermeb.expensevault.boundary.dto.response.ExpenseSummaryResponse;
import dev.bermeb.expensevault.control.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Endpoints for generating and retrieving expense reports")
public class ReportController {

    private final ReportService reportService;

    // TODO: Add period presets (e.g., last month, last quarter) and category filtering
    @GetMapping("/summary")
    @Operation(summary = "Get Expense Summary", description = "Retrieve a summary of expenses over a specified period.")
    public ResponseEntity<ExpenseSummaryResponse> getExpenseSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category) {

        // Since there is no category filtering implemented yet, we ignore the category parameter
        log.info("Generating expense summary from {} to {}", startDate, endDate);

        ExpenseSummaryResponse summary = reportService.generateSummary(startDate, endDate);

        return ResponseEntity.ok(summary);
    }
}