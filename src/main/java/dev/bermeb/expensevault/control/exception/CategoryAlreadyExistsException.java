package dev.bermeb.expensevault.control.exception;

public class CategoryAlreadyExistsException extends BaseException {

    private static final String ERROR_CODE = "CATEGORY_ALREADY_EXISTS";

    public CategoryAlreadyExistsException(String categoryName) {
        super("Category with name '" + categoryName + "' already exists");
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}