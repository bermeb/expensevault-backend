package dev.bermeb.expensevault.control.exception;

import java.util.UUID;

public class CategoryHasReceiptsException extends BaseException {

    private static final String ERROR_CODE = "CATEGORY_HAS_RECEIPTS";

    public CategoryHasReceiptsException(UUID categoryId) {
        super("Cannot delete category with existing receipts");
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}