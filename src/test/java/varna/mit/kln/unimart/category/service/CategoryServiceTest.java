package varna.mit.kln.unimart.category.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import varna.mit.kln.unimart.category.dto.CategoryRequestDto;
import varna.mit.kln.unimart.category.dto.CategoryResponseDto;
import varna.mit.kln.unimart.category.entity.Category;
import varna.mit.kln.unimart.category.repository.CategoryRepository;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository);
    }

    @Test
    void createCategory_Success() {
        CategoryRequestDto request = new CategoryRequestDto("Textbooks", true);

        Category savedCategory = new Category();
        savedCategory.setId(1);
        savedCategory.setName("Textbooks");
        savedCategory.setActive(true);

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponseDto response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Textbooks", response.getName());
        assertTrue(response.getActive());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void getAllActiveCategories_Success() {
        Category cat1 = new Category();
        cat1.setId(1);
        cat1.setName("Textbooks");
        cat1.setActive(true);

        Category cat2 = new Category();
        cat2.setId(2);
        cat2.setName("Electronics");
        cat2.setActive(true);

        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(cat1, cat2));

        List<CategoryResponseDto> categories = categoryService.getAllActiveCategories();

        assertEquals(2, categories.size());
        assertEquals("Textbooks", categories.get(0).getName());
        assertEquals("Electronics", categories.get(1).getName());
    }

    @Test
    void getCategoryById_Success() {
        Category cat = new Category();
        cat.setId(1);
        cat.setName("Textbooks");
        cat.setActive(true);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));

        CategoryResponseDto response = categoryService.getCategoryById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Textbooks", response.getName());
    }

    @Test
    void getCategoryById_NotFound_ThrowsResourceNotFoundException() {
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99));
    }

    @Test
    void updateCategory_Success() {
        Category cat = new Category();
        cat.setId(1);
        cat.setName("Textbooks");
        cat.setActive(true);

        CategoryRequestDto updateRequest = new CategoryRequestDto("Course Textbooks", true);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(any(Category.class))).thenReturn(cat);

        CategoryResponseDto response = categoryService.updateCategory(1, updateRequest);

        assertNotNull(response);
        assertEquals("Course Textbooks", response.getName());
        verify(categoryRepository, times(1)).save(cat);
    }

    @Test
    void deleteCategory_SoftDeactivatesCategory() {
        Category cat = new Category();
        cat.setId(1);
        cat.setName("Textbooks");
        cat.setActive(true);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));

        categoryService.deleteCategory(1);

        assertFalse(cat.getActive());
        verify(categoryRepository, times(1)).save(cat);
    }
}
