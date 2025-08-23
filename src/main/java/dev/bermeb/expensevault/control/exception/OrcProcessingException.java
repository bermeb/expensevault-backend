package dev.bermeb.expensevault.control.exception;

public class OrcProcessingException extends BaseException {

    private static final String ERROR_CODE = "ORC_PROCESSING_FAILED";

    public OrcProcessingException(String message) {
        super(message);
    }

    public OrcProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}