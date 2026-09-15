package varna.mit.kln.unimart.category.service;

import java.util.List;
import varna.mit.kln.unimart.category.entity.Category;

public interface CategoryService {
    Category createCategory(Category category);
    List<Category> getAllActiveCategories();
}
