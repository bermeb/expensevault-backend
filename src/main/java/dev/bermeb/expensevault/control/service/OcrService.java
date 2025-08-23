package dev.bermeb.expensevault.control.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import dev.bermeb.expensevault.boundary.dto.response.OcrResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Coded with Docs and Claude.ai
 * TODO: Needs testing with different receipt formats and edge cases
 */
@Service
@Slf4j
public class OcrService {

    private final ImageAnnotatorClient visionClient;

    // Regex patterns for German receipts
    // TODO: Add more patterns for different countries and edge cases
    private static final Pattern TOTAL_PATTERN = Pattern.compile(
            "(?i)(?:summe|gesamt|total|sum|betrag|endsumme|zu\\s+zahlen)\\s*:?\\s*(\\d+[,.]\\d{2})",
            Pattern.MULTILINE
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(\\d{1,2})[./](\\d{1,2})[./](\\d{2,4})",
            Pattern.MULTILINE
    );

    // TODO: Check if it detects merchant names correctly
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
            "^([A-ZÄÖÜ][A-ZÄÖÜa-zäöüß\\s&\\-.]{2,50})",
            Pattern.MULTILINE
    );

    // TODO: Handle different currency formats
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(\\d{1,3}(?:[,.]\\d{3})*[,.]\\d{2})\\s*€?\\s*EUR?",
            Pattern.MULTILINE
    );

    public OcrService() throws IOException {
        this.visionClient = ImageAnnotatorClient.create();
        log.info("Google Vision OCR Service initialized successfully");
    }

    // TODO: Add "multiple images"-receipt handling
    public OcrResult extractReceiptData(byte[] imageData) throws Exception {
        try {
            log.debug("Starting OCR processing for image of size: {} bytes", imageData.length);

            ByteString imgBytes = ByteString.copyFrom(imageData);
            var img = Image.newBuilder().setContent(imgBytes).build();

            var textFeature = Feature.newBuilder()
                    .setType(Feature.Type.TEXT_DETECTION)
                    .build();

            var annotateImageRequest = AnnotateImageRequest.newBuilder()
                    .addFeatures(textFeature)
                    .setImage(img)
                    .build();

            BatchAnnotateImagesResponse response = visionClient
                    .batchAnnotateImages(List.of(annotateImageRequest));

            AnnotateImageResponse imgResponse = response.getResponses(0);

            if (imgResponse.hasError()) {
                throw new Exception("Google Vision API error: " +
                        imgResponse.getError().getMessage()); // TODO: Replace with custom exception
            }

            if (imgResponse.getTextAnnotationsList().isEmpty()) {
                throw new Exception("No text detected in image"); // TODO: Replace with custom exception
            }

            String extractedText = imgResponse.getTextAnnotations(0).getDescription();
            float confidence = calculateAverageConfidence(imgResponse.getTextAnnotationsList());

            log.debug("OCR extraction completed with confidence: {}", confidence);

           return parseReceiptText(extractedText, confidence);
        } catch (Exception e) {
            log.error("OCR processing failed: {}", e.getMessage());
            throw new Exception("Failed to process image for OCR", e); // TODO: Replace with custom exception
        }
    }

    private OcrResult parseReceiptText(String text, float confidence) {
        log.debug("Parsing extracted text: {}", text.substring(0, Math.min(text.length(), 100)) + "...");

        OcrResult.OcrResultBuilder builder = OcrResult.builder()
                .rawText(text)
                .confidence(confidence);

        // Extract total amount
        BigDecimal totalAmount = extractTotalAmount(text);
        builder.totalAmount(totalAmount);

        // Extract date
        LocalDate date = extractDate(text);
        builder.date(date);

        // Extract merchant name
        String merchantName = extractMerchantName(text);
        builder.merchantName(merchantName);

        // Extract detected items/amounts
        List<String> detectedItems = extractItems(text);
        builder.detectedItems(detectedItems);

        OcrResult result = builder.build();
        log.info("Parsed receipt - Amount: {}, Date: {}, Merchant: {}",
                totalAmount, date, merchantName);

        return result;
    }

    private BigDecimal extractTotalAmount(String text) {
        Matcher matcher = TOTAL_PATTERN.matcher(text);
        if (matcher.find()) {
            // Replace comma with dot for BigDecimal parsing
            String amountStr = matcher.group(1).replace(",", ".");
            try {
                return new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse total amount: {}", amountStr);
            }
        }

        return findLargestAmount(text);
    }

    private BigDecimal findLargestAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        BigDecimal maxAmount = BigDecimal.ZERO;

        while (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", ".");
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                if (amount.compareTo(maxAmount) > 0) {
                    maxAmount = amount;
                }
            } catch (NumberFormatException e) {
                log.debug("Skipped invalid amount: {}", amountStr);
            }
        }

        return maxAmount.compareTo(BigDecimal.ZERO) > 0 ? maxAmount : null;
    }

    private LocalDate extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            String day = matcher.group(1);
            String month = matcher.group(2);
            String year = matcher.group(3);

            // Handle 2-digit years
            if (year.length() == 2) {
                int yearInt = Integer.parseInt(year);
                year = String.valueOf(yearInt < 50 ? 2000 + yearInt : 1900 + yearInt);
            }

            try {
                String dateStr = String.format("%s-%s-%s", year,
                        month.length() == 1 ? "0" + month : month,
                        day.length() == 1 ? "0" + day : day);
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse date: {}/{}/{}", day, month, year);
            }
        }

        return null;
    }

    private String extractMerchantName(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            Matcher matcher = MERCHANT_PATTERN.matcher(line);
            if (matcher.find() && line.length() < 50) {
                String merchant = matcher.group(1).trim();
                if (!merchant.matches("\\d+") && merchant.length() > 2) {
                    return merchant;
                }
            }
        }
        return null;
    }

    private List<String> extractItems(String text) {
        List<String> items = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();
            // Look for lines that contain both text and amounts
            if (line.matches(".*[a-zA-ZäöüÄÖÜß].*\\d+[,.]\\d{2}.*")) {
                items.add(line);
            }
        }

        return items;
    }

    private float calculateAverageConfidence(List<EntityAnnotation> annotations) {
        if(annotations.isEmpty()) return 0.0f;

        float totalConfidence = 0.0f;
        int count = 0;

        for(EntityAnnotation annotation : annotations) {
            // Score since confidence is deprecated
            // Score is for overall qualitiy of the OCR result
            // Confidence was only for text detection
            if(annotation.getScore() > 0) {
                totalConfidence += annotation.getScore();
                count++;
            }
        }

        if(count == 0) {
            log.warn("No valid confidence scores found in annotations");
            return 0.0f;
        }

        // Return average confidence for all annotations
        return totalConfidence / count;
    }
}
