package dev.bermeb.expensevault.boundary.mapper;

import dev.bermeb.expensevault.boundary.dto.response.ReceiptResponse;
import dev.bermeb.expensevault.entity.model.Receipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {CategoryMapper.class, OcrDataMapper.class},
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE
)
public interface ReceiptMapper {

    @Mapping(target = "category", source = "category")
    @Mapping(target = "ocrData", source = "ocrData")
    ReceiptResponse toResponse(Receipt receipt);
}
