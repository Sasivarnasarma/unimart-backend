package varna.mit.kln.unimart.payment.controller;

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
import varna.mit.kln.unimart.payment.dto.PaymentRequestDto;
import varna.mit.kln.unimart.payment.entity.Payment;
import varna.mit.kln.unimart.payment.entity.PaymentStatus;
import varna.mit.kln.unimart.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class PaymentControllerTest {

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
    private PaymentRepository paymentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private User testBuyer;
    private User testSeller;
    private Order testOrder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testBuyer = userRepository.findByUniversityEmail("pay_ctrl_buyer@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("pay_ctrl_buyer@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Pay Ctrl Buyer");
                    u.setRole(UserRole.buyer);
                    return userRepository.save(u);
                });

        testSeller = userRepository.findByUniversityEmail("pay_ctrl_seller@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("pay_ctrl_seller@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Pay Ctrl Seller");
                    u.setRole(UserRole.seller);
                    return userRepository.save(u);
                });

        Category cat = categoryRepository.findByActiveTrue().stream().findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("PayCategory_" + System.currentTimeMillis());
                    c.setActive(true);
                    return categoryRepository.save(c);
                });

        Listing testListing = new Listing();
        testListing.setSeller(testSeller);
        testListing.setCategory(cat);
        testListing.setTitle("Payment Test Item");
        testListing.setPrice(new BigDecimal("120.00"));
        testListing.setStatus(ListingStatus.available);
        testListing = listingRepository.save(testListing);

        Order order = new Order(testListing, testBuyer, new BigDecimal("120.00"), "CARD");
        order.setStatus(OrderStatus.pending);
        testOrder = orderRepository.save(order);

        String idempotencyKey = "TEST-IDEM-KEY-" + UUID.randomUUID();
        Payment payment = new Payment(testOrder, new BigDecimal("120.00"), PaymentStatus.success, "TXN-001", idempotencyKey);
        testPayment = paymentRepository.save(payment);
    }

    @Test
    @WithMockUser(username = "pay_ctrl_buyer@kln.ac.lk")
    void processPayment_Authenticated_Returns201Created() throws Exception {
        Listing freshListing = new Listing();
        freshListing.setSeller(testSeller);
        freshListing.setCategory(testOrder.getListing().getCategory());
        freshListing.setTitle("Item for Payment Processing");
        freshListing.setPrice(new BigDecimal("80.00"));
        freshListing.setStatus(ListingStatus.available);
        freshListing = listingRepository.save(freshListing);

        Order newOrder = new Order(freshListing, testBuyer, new BigDecimal("80.00"), "CARD");
        newOrder.setStatus(OrderStatus.pending);
        newOrder = orderRepository.save(newOrder);

        String idempotencyKey = "NEW-IDEM-KEY-" + UUID.randomUUID();
        PaymentRequestDto request = new PaymentRequestDto(newOrder.getId(), new BigDecimal("80.00"), idempotencyKey, "MOCK-PROV-1");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value(newOrder.getId()))
                .andExpect(jsonPath("$.amount").value(80.00))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.idempotencyKey").value(idempotencyKey));
    }

    @Test
    @WithMockUser(username = "pay_ctrl_buyer@kln.ac.lk")
    void getPaymentByOrderId_Authenticated_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/payments/order/" + testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPayment.getId()))
                .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.status").value("success"));
    }
}
