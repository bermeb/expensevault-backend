package dev.bermeb.expensevault.boundary.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.bermeb.expensevault.boundary.dto.response.OcrDataResponse;
import dev.bermeb.expensevault.entity.model.OcrData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class OcrDataMapper {

    private final ObjectMapper objectMapper;

    protected OcrDataMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Mapping(target = "extractedFields", source = "extractedFields", qualifiedByName = "jsonToMap")
    public abstract OcrDataResponse toResponse(OcrData ocrData);

    @Named("jsonToMap")
    protected Map<String, Object> jsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
    }

}
