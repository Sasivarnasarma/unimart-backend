package varna.mit.kln.unimart.category.service;

import java.util.List;
import varna.mit.kln.unimart.category.dto.CategoryRequestDto;
import varna.mit.kln.unimart.category.dto.CategoryResponseDto;

public interface CategoryService {
    CategoryResponseDto createCategory(CategoryRequestDto requestDto);
    List<CategoryResponseDto> getAllActiveCategories();
    CategoryResponseDto getCategoryById(Integer id);
    CategoryResponseDto updateCategory(Integer id, CategoryRequestDto requestDto);
    void deleteCategory(Integer id);
}
