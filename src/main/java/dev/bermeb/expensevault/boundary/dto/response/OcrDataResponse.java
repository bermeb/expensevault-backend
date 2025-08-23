package dev.bermeb.expensevault.boundary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrDataResponse {
    private String rawText;
    private Float confidence;
    private LocalDateTime processedAt;
    private Map<String, Object> extractedFields;
}