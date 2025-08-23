package dev.bermeb.expensevault.control.exception;

public class ReceiptProcessingException extends BaseException {

    private static final String ERROR_CODE = "RECEIPT_PROCESSING_FAILED";

    public ReceiptProcessingException(String message) {
        super(message);
    }

    public ReceiptProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}