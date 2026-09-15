package varna.mit.kln.unimart.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import varna.mit.kln.unimart.category.dto.CategoryRequestDto;
import varna.mit.kln.unimart.category.entity.Category;
import varna.mit.kln.unimart.category.repository.CategoryRepository;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class CategoryControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testCategory = categoryRepository.findByActiveTrue().stream().findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("TestCategory_" + System.currentTimeMillis());
                    c.setActive(true);
                    return categoryRepository.save(c);
                });
    }

    @Test
    @WithMockUser
    void createCategory_ValidPayload_Returns201Created() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Category_" + System.currentTimeMillis(), true);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getAllActiveCategories_PublicEndpoint_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getCategoryById_PublicEndpoint_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/categories/" + testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCategory.getId()));
    }

    @Test
    @WithMockUser
    void updateCategory_ValidPayload_Returns200OK() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto("Updated_Cat_" + System.currentTimeMillis(), true);

        mockMvc.perform(put("/api/v1/categories/" + testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCategory.getId()))
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    @WithMockUser
    void deleteCategory_DeactivatesCategory_Returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/" + testCategory.getId()))
                .andExpect(status().isNoContent());
    }
}
