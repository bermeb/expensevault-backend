package dev.bermeb.expensevault.boundary.controller;

import dev.bermeb.expensevault.boundary.dto.request.CategoryCreateRequest;
import dev.bermeb.expensevault.boundary.dto.request.CategoryUpdateRequest;
import dev.bermeb.expensevault.boundary.dto.response.CategoryResponse;
import dev.bermeb.expensevault.boundary.mapper.CategoryMapper;
import dev.bermeb.expensevault.control.exception.CategoryNotFoundException;
import dev.bermeb.expensevault.control.service.CategoryService;
import dev.bermeb.expensevault.entity.model.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Endpoints for managing expense categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    @Operation(summary = "Get All Categories", description = "Retrieve a list of all expense categories.")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        List<CategoryResponse> responses = categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get Category by ID", description = "Retrieve a specific category by its ID.")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable UUID categoryId) {
        Category category = categoryService.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @PostMapping
    @Operation(summary = "Create Category", description = "Create a new expense category.")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Category category = categoryService.create(request);

        log.info("Created Category with ID: {} and name: {}", category.getId(), category.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryMapper.toResponse(category));
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update Category", description = "Update an existing expense category.")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequest request) {
        Category category = categoryService.update(categoryId, request);

        log.info("Updated Category with ID: {}", category.getId());

        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete Category", description = "Delete an expense category by its ID.")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteById(categoryId);

        log.info("Deleted Category with ID: {}", categoryId);

        return ResponseEntity.noContent().build();
    }
}