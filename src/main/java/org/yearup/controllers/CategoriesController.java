package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin // Allows requests from other origins, e.g., a frontend app
public class CategoriesController {

    private final CategoryDao categoryDao;

    @Autowired
    public CategoriesController(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    // GET http://localhost:8080/categories
    @GetMapping("")
    public List<Category> getAll() {
        // Find and return all categories
        return categoryDao.getAllCategories();
    }

    // GET http://localhost:8080/categories/1
    @GetMapping("/{id}")
    public Category getById(@PathVariable int id) {
        // Find the category by its ID
        Category category = categoryDao.getById(id);

        // If the category is not found, return a 404 Not Found error
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        return category;
    }

    // POST http://localhost:8080/categories
    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Category addCategory(@RequestBody Category category) {
        // Create a new category and return it with its new ID
        try {
            return categoryDao.create(category);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", ex);
        }
    }

    // PUT http://localhost:8080/categories/1
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategory(@PathVariable int id, @RequestBody Category category) {
        // Update the category by its ID
        try {
            // Check if category exists before updating
            if (categoryDao.getById(id) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category to update was not found.");
            }
            categoryDao.update(id, category);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", ex);
        }
    }

    // DELETE http://localhost:8080/categories/1
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable int id) {
        // Delete the category by its ID
        try {
            // Check if category exists before deleting
            if (categoryDao.getById(id) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category to delete was not found.");
            }
            categoryDao.delete(id);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", ex);
        }
    }
}