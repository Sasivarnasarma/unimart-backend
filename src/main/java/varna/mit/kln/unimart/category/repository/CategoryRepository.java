package varna.mit.kln.unimart.category.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByActiveTrue();
}
