package varna.mit.kln.unimart.listing.controller;

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
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.entity.UserRole;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.category.entity.Category;
import varna.mit.kln.unimart.category.repository.CategoryRepository;
import varna.mit.kln.unimart.listing.dto.ListingImageRequestDto;
import varna.mit.kln.unimart.listing.dto.ListingRequestDto;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.repository.ListingRepository;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class ListingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ListingRepository listingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private User testSeller;
    private Category testCategory;
    private Listing testListing;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testSeller = userRepository.findByUniversityEmail("seller_test@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("seller_test@kln.ac.lk");
                    u.setPasswordHash("hashedpass");
                    u.setFullName("Seller Test");
                    u.setRole(UserRole.seller);
                    return userRepository.save(u);
                });

        testCategory = categoryRepository.findByActiveTrue().stream().findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("TestCategory_" + System.currentTimeMillis());
                    c.setActive(true);
                    return categoryRepository.save(c);
                });

        testListing = new Listing();
        testListing.setSeller(testSeller);
        testListing.setCategory(testCategory);
        testListing.setTitle("Test Listing Title");
        testListing.setDescription("Test Listing Description");
        testListing.setPrice(new BigDecimal("199.99"));
        testListing = listingRepository.save(testListing);
    }

    @Test
    void searchListings_PublicEndpoint_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/listings")
                        .param("q", "Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getListingById_PublicEndpoint_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/listings/" + testListing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testListing.getId()))
                .andExpect(jsonPath("$.title").value("Test Listing Title"));
    }

    @Test
    @WithMockUser(username = "seller_test@kln.ac.lk")
    void createListing_Authenticated_Returns201Created() throws Exception {
        ListingRequestDto request = new ListingRequestDto();
        request.setCategoryId(testCategory.getId());
        request.setTitle("New Tech Laptop");
        request.setDescription("Excellent condition laptop");
        request.setPrice(new BigDecimal("45000.00"));

        mockMvc.perform(post("/api/v1/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Tech Laptop"))
                .andExpect(jsonPath("$.sellerName").value("Seller Test"));
    }

    @Test
    @WithMockUser(username = "seller_test@kln.ac.lk")
    void updateListing_Authenticated_Returns200OK() throws Exception {
        ListingRequestDto request = new ListingRequestDto();
        request.setCategoryId(testCategory.getId());
        request.setTitle("Updated Tech Laptop Title");
        request.setPrice(new BigDecimal("42000.00"));

        mockMvc.perform(put("/api/v1/listings/" + testListing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Tech Laptop Title"));
    }

    @Test
    @WithMockUser(username = "seller_test@kln.ac.lk")
    void addImageAndRemoveImage_Authenticated_Success() throws Exception {
        ListingImageRequestDto imageDto = new ListingImageRequestDto("http://example.com/gallery.jpg", 1);

        String responseContent = mockMvc.perform(post("/api/v1/listings/" + testListing.getId() + "/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imageDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/gallery.jpg"))
                .andReturn().getResponse().getContentAsString();

        Integer imageId = objectMapper.readTree(responseContent).get("id").asInt();

        mockMvc.perform(delete("/api/v1/listings/" + testListing.getId() + "/images/" + imageId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "seller_test@kln.ac.lk")
    void archiveListing_Authenticated_Returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/listings/" + testListing.getId()))
                .andExpect(status().isNoContent());
    }
}
