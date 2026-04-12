package com.example.coreservice.controller;

import com.example.coreservice.dto.ApiResponse;
import com.example.coreservice.entity.Category;
import com.example.coreservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 📂 Get all categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        // Matches service.getAllCategories()
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully", categories));
    }

    /**
     * 🔍 Get a single category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getById(@PathVariable Long id) {
        // Matches service.getCategoryById(id)
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved", category));
    }

    /**
     * ✨ Create or Update a category
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Category category) {
        // Matches service.saveCategory(category)
        Category savedCategory = categoryService.saveCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", savedCategory));
    }

    /**
     * 🗑️ Delete a category
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        // Matches service.deleteCategory(id)
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}