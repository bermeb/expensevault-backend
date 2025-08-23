package dev.bermeb.expensevault.control.service;

import dev.bermeb.expensevault.boundary.dto.request.CategoryCreateRequest;
import dev.bermeb.expensevault.boundary.dto.request.CategoryUpdateRequest;
import dev.bermeb.expensevault.entity.model.Category;
import dev.bermeb.expensevault.entity.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(UUID id) {
        return categoryRepository.findById(id);
    }

    public Category create(CategoryCreateRequest request) throws Exception {
        if (categoryRepository.existsByName(request.getName())) {
            throw new Exception("Category with name " + request.getName() + " already exists"); // TODO: Replace with custom exception
        }

        Category category = Category.builder()
                .name(request.getName())
                .color(request.getColor())
                .icon(request.getIcon())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created with ID: {} and name: {}", savedCategory.getId(), savedCategory.getName());
        return savedCategory;
    }

    public Category update(UUID id, CategoryUpdateRequest request) throws Exception {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Category with this ID not found: " + id));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new Exception("Category with name " + request.getName() + " already exists"); // TODO: Replace with custom exception
            }
            category.setName(request.getName());
        }

        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }

        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated with ID: {}", updatedCategory.getId());
        return updatedCategory;
    }

    public void deleteById(UUID id) throws Exception {
        if (!categoryRepository.existsById(id)) {
            throw new Exception("Category with this ID not found: " + id); // TODO: Replace with custom exception
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted with ID: {}", id);
    }
}
