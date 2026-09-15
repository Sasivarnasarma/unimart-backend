package varna.mit.kln.unimart.review.controller;

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
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingStatus;
import varna.mit.kln.unimart.listing.repository.ListingRepository;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;
import varna.mit.kln.unimart.order.repository.OrderRepository;
import varna.mit.kln.unimart.review.dto.ReviewRequestDto;
import varna.mit.kln.unimart.review.dto.ReviewUpdateDto;
import varna.mit.kln.unimart.review.entity.Review;
import varna.mit.kln.unimart.review.repository.ReviewRepository;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class ReviewControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private User testBuyer;
    private User testSeller;
    private Order completedOrder;
    private Review testReview;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testBuyer = userRepository.findByUniversityEmail("rev_ctrl_buyer@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("rev_ctrl_buyer@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Review Ctrl Buyer");
                    u.setRole(UserRole.buyer);
                    return userRepository.save(u);
                });

        testSeller = userRepository.findByUniversityEmail("rev_ctrl_seller@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("rev_ctrl_seller@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Review Ctrl Seller");
                    u.setRole(UserRole.seller);
                    return userRepository.save(u);
                });

        Category cat = categoryRepository.findByActiveTrue().stream().findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("RevCategory_" + System.currentTimeMillis());
                    c.setActive(true);
                    return categoryRepository.save(c);
                });

        Listing testListing = new Listing();
        testListing.setSeller(testSeller);
        testListing.setCategory(cat);
        testListing.setTitle("Review Test Listing Item");
        testListing.setPrice(new BigDecimal("150.00"));
        testListing.setStatus(ListingStatus.sold);
        testListing = listingRepository.save(testListing);

        Order order = new Order(testListing, testBuyer, new BigDecimal("150.00"), "CARD");
        order.setStatus(OrderStatus.completed);
        completedOrder = orderRepository.save(order);

        testReview = reviewRepository.findByOrderId(completedOrder.getId())
                .orElseGet(() -> {
                    Review r = new Review(completedOrder, testBuyer, testSeller, 5, "Amazing seller experience!");
                    return reviewRepository.save(r);
                });
    }

    @Test
    @WithMockUser(username = "rev_ctrl_buyer@kln.ac.lk")
    void createReview_Authenticated_Returns201Created() throws Exception {
        Listing freshListing = new Listing();
        freshListing.setSeller(testSeller);
        freshListing.setCategory(completedOrder.getListing().getCategory());
        freshListing.setTitle("Second Item for Review Test");
        freshListing.setPrice(new BigDecimal("60.00"));
        freshListing.setStatus(ListingStatus.sold);
        freshListing = listingRepository.save(freshListing);

        Order newCompletedOrder = new Order(freshListing, testBuyer, new BigDecimal("60.00"), "CARD");
        newCompletedOrder.setStatus(OrderStatus.completed);
        newCompletedOrder = orderRepository.save(newCompletedOrder);

        ReviewRequestDto request = new ReviewRequestDto(newCompletedOrder.getId(), 4, "Good transaction!");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value(newCompletedOrder.getId()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Good transaction!"));
    }

    @Test
    @WithMockUser(username = "rev_ctrl_buyer@kln.ac.lk")
    void getSellerReviews_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/seller/" + testSeller.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "rev_ctrl_buyer@kln.ac.lk")
    void getSellerRatingSummary_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/seller/" + testSeller.getId() + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerId").value(testSeller.getId()))
                .andExpect(jsonPath("$.averageRating").exists())
                .andExpect(jsonPath("$.totalReviews").exists());
    }

    @Test
    @WithMockUser(username = "rev_ctrl_buyer@kln.ac.lk")
    void updateReview_Authenticated_Returns200OK() throws Exception {
        ReviewUpdateDto updateDto = new ReviewUpdateDto(5, "Updated: phenomenal seller!");

        mockMvc.perform(put("/api/v1/reviews/" + testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Updated: phenomenal seller!"));
    }

    @Test
    @WithMockUser(username = "rev_ctrl_buyer@kln.ac.lk")
    void deleteReview_Authenticated_Returns204NoContent() throws Exception {
        Order deleteTestOrder = new Order(completedOrder.getListing(), testBuyer, new BigDecimal("10.00"), "CARD");
        deleteTestOrder.setStatus(OrderStatus.completed);
        deleteTestOrder = orderRepository.save(deleteTestOrder);

        Review reviewToDelete = reviewRepository.save(new Review(deleteTestOrder, testBuyer, testSeller, 3, "To be deleted"));

        mockMvc.perform(delete("/api/v1/reviews/" + reviewToDelete.getId()))
                .andExpect(status().isNoContent());
    }
}
