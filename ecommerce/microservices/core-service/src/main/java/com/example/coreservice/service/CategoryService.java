package com.example.coreservice.service;

import com.example.coreservice.entity.Category;
import com.example.coreservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 📂 Fetch all active categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        log.info("Fetching all product categories");
        return categoryRepository.findAll();
    }

    /**
     * 🔍 Get a specific category by ID
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    /**
     * ✨ Create or Update a category
     */
    public Category saveCategory(Category category) {
        log.info("Saving category: {}", category.getName());
        return categoryRepository.save(category);
    }

    /**
     * 🗑️ Delete a category
     */
    public void deleteCategory(Long id) {
        log.warn("Deleting category ID: {}", id);
        categoryRepository.deleteById(id);
    }
}