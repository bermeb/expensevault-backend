package dev.bermeb.expensevault.control.exception;

public class InvalidFileException extends BaseException {

    private static final String ERROR_CODE = "INVALID_FILE";

    public InvalidFileException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
