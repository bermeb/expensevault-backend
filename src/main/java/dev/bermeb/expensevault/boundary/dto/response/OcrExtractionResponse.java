package dev.bermeb.expensevault.boundary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrExtractionResponse {
    private String rawText;
    private Float confidence;
    private Map<String, Object> extractedData;
    private Integer processingTimeMs;
}