package dev.bermeb.expensevault.control.exception;

import java.util.UUID;

public class CategoryNotFoundException extends BaseException {

    private static final String ERROR_CODE = "CATEGORY_NOT_FOUND";

    public CategoryNotFoundException(UUID categoryId) {
        super("Category with ID " + categoryId + " not found.");
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}