package varna.mit.kln.unimart.category.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import varna.mit.kln.unimart.category.dto.CategoryRequestDto;
import varna.mit.kln.unimart.category.dto.CategoryResponseDto;
import varna.mit.kln.unimart.category.entity.Category;
import varna.mit.kln.unimart.category.repository.CategoryRepository;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        Category category = new Category();
        category.setName(requestDto.getName());
        category.setActive(requestDto.getActive() != null ? requestDto.getActive() : true);
        Category savedCategory = categoryRepository.save(category);
        return new CategoryResponseDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(CategoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return new CategoryResponseDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategory(Integer id, CategoryRequestDto requestDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        category.setName(requestDto.getName());
        if (requestDto.getActive() != null) {
            category.setActive(requestDto.getActive());
        }
        Category updatedCategory = categoryRepository.save(category);
        return new CategoryResponseDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
