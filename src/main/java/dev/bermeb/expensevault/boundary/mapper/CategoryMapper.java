package dev.bermeb.expensevault.boundary.mapper;

import dev.bermeb.expensevault.boundary.dto.response.CategoryResponse;
import dev.bermeb.expensevault.entity.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE
)
public interface CategoryMapper {

    @Mapping(target = "receiptCount", expression = "java(category.getReceipts() != null ? category.getReceipts().size() : 0)")
    CategoryResponse toResponse(Category category);

    @Mapping(target = "receiptCount", ignore = true)
    CategoryResponse toResponseWithoutCount(Category category);
}
