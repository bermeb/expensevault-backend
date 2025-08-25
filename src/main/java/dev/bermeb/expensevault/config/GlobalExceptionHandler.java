package dev.bermeb.expensevault.config;

import dev.bermeb.expensevault.boundary.dto.response.ErrorResponse;
import dev.bermeb.expensevault.control.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReceiptNotFound(ReceiptNotFoundException ex, WebRequest request) {
        log.warn("Receipt not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex, WebRequest request) {
        log.warn("Category not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryAlreadyExists(CategoryAlreadyExistsException ex, WebRequest request) {
        log.warn("Category already exists: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CategoryHasReceiptsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryHasReceipts(CategoryHasReceiptsException ex, WebRequest request) {
        log.warn("Category deletion failed: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OrcProcessingException.class)
    public ResponseEntity<ErrorResponse> handleOrcProcessing(OrcProcessingException ex, WebRequest request) {
        log.error("OCR processing failed: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex, WebRequest request) {
        log.warn("Invalid file upload: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReceiptProcessingException.class)
    public ResponseEntity<ErrorResponse> handleReceiptProcessing(ReceiptProcessingException ex, WebRequest request) {
        log.error("Receipt processing failed: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) ->  {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        // TODO: Better handling of multiple validation errors
        ErrorResponse error = ErrorResponse.builder()
                .error("VALIDATION_FAILED")
                .message("Validation failed: " + errors)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeException(MaxUploadSizeExceededException ex, WebRequest request) {
        log.warn("File size exceeded limit: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("FILE_SIZE_EXCEEDED")
                .message("File size exceeds the maximum allowed limit for files.")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse error = ErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred.")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}