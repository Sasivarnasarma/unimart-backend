package varna.mit.kln.unimart.order.controller;

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
import varna.mit.kln.unimart.order.dto.OrderRequestDto;
import varna.mit.kln.unimart.order.dto.OrderStatusUpdateDto;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;
import varna.mit.kln.unimart.order.repository.OrderRepository;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class OrderControllerTest {

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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private User testBuyer;
    private User testSeller;
    private Listing testListing;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testBuyer = userRepository.findByUniversityEmail("order_ctrl_buyer@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("order_ctrl_buyer@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Order Ctrl Buyer");
                    u.setRole(UserRole.buyer);
                    return userRepository.save(u);
                });

        testSeller = userRepository.findByUniversityEmail("order_ctrl_seller@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("order_ctrl_seller@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Order Ctrl Seller");
                    u.setRole(UserRole.seller);
                    return userRepository.save(u);
                });

        Category cat = categoryRepository.findByActiveTrue().stream().findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("OrderCategory_" + System.currentTimeMillis());
                    c.setActive(true);
                    return categoryRepository.save(c);
                });

        testListing = new Listing();
        testListing.setSeller(testSeller);
        testListing.setCategory(cat);
        testListing.setTitle("Order Test Item");
        testListing.setPrice(new BigDecimal("50.00"));
        testListing.setStatus(ListingStatus.available);
        testListing = listingRepository.save(testListing);

        Order order = new Order(testListing, testBuyer, new BigDecimal("50.00"), "CARD");
        order.setStatus(OrderStatus.pending);
        testOrder = orderRepository.save(order);
    }

    @Test
    @WithMockUser(username = "order_ctrl_buyer@kln.ac.lk")
    void createOrder_Authenticated_Returns201Created() throws Exception {
        Listing freshListing = new Listing();
        freshListing.setSeller(testSeller);
        freshListing.setCategory(testListing.getCategory());
        freshListing.setTitle("Fresh Listing for Order");
        freshListing.setPrice(new BigDecimal("30.00"));
        freshListing.setStatus(ListingStatus.available);
        freshListing = listingRepository.save(freshListing);

        OrderRequestDto request = new OrderRequestDto(freshListing.getId(), "CARD");

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.listingId").value(freshListing.getId()))
                .andExpect(jsonPath("$.totalAmount").value(30.00))
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    @WithMockUser(username = "order_ctrl_buyer@kln.ac.lk")
    void getUserOrders_Authenticated_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "order_ctrl_buyer@kln.ac.lk")
    void getOrderById_Authenticated_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/orders/" + testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.buyerId").value(testBuyer.getId()));
    }

    @Test
    @WithMockUser(username = "order_ctrl_seller@kln.ac.lk")
    void updateOrderStatus_Authenticated_Returns200OK() throws Exception {
        OrderStatusUpdateDto updateDto = new OrderStatusUpdateDto(OrderStatus.completed);

        mockMvc.perform(patch("/api/v1/orders/" + testOrder.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"));
    }
}
