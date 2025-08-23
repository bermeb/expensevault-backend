package dev.bermeb.expensevault.control.exception;

import java.util.UUID;

public class ReceiptNotFoundException extends BaseException {

    private static final String ERROR_CODE = "RECEIPT_NOT_FOUND";

    public ReceiptNotFoundException(UUID uuid) {
        super("Receipt with ID " + uuid + " not found.");
    }

    public ReceiptNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}