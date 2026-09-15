package varna.mit.kln.unimart.category.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.category.dto.CategoryRequestDto;
import varna.mit.kln.unimart.category.dto.CategoryResponseDto;
import varna.mit.kln.unimart.category.service.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@Valid @RequestBody CategoryRequestDto requestDto) {
        CategoryResponseDto responseDto = categoryService.createCategory(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllActive() {
        List<CategoryResponseDto> categories = categoryService.getAllActiveCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getById(@PathVariable Integer id) {
        CategoryResponseDto responseDto = categoryService.getCategoryById(id);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable Integer id, @Valid @RequestBody CategoryRequestDto requestDto) {
        CategoryResponseDto responseDto = categoryService.updateCategory(id, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
