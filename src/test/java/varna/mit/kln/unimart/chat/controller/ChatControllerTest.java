package varna.mit.kln.unimart.chat.controller;

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
import varna.mit.kln.unimart.chat.dto.ConversationRequestDto;
import varna.mit.kln.unimart.chat.dto.MessageRequestDto;
import varna.mit.kln.unimart.chat.entity.Conversation;
import varna.mit.kln.unimart.chat.repository.ConversationRepository;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.repository.ListingRepository;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class ChatControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private User testBuyer;
    private User testSeller;
    private Listing testListing;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testBuyer = userRepository.findByUniversityEmail("chat_buyer@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("chat_buyer@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Chat Buyer");
                    u.setRole(UserRole.buyer);
                    return userRepository.save(u);
                });

        testSeller = userRepository.findByUniversityEmail("chat_seller@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("chat_seller@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Chat Seller");
                    u.setRole(UserRole.seller);
                    return userRepository.save(u);
                });

        Category cat = categoryRepository.findByActiveTrue().stream().findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("ChatCategory_" + System.currentTimeMillis());
                    c.setActive(true);
                    return categoryRepository.save(c);
                });

        testListing = new Listing();
        testListing.setSeller(testSeller);
        testListing.setCategory(cat);
        testListing.setTitle("Chat Listing Test Item");
        testListing.setPrice(new BigDecimal("99.99"));
        testListing = listingRepository.save(testListing);

        Listing finalListing = testListing;
        testConversation = conversationRepository.findByListingIdAndBuyerId(testListing.getId(), testBuyer.getId())
                .orElseGet(() -> conversationRepository.save(new Conversation(finalListing, testBuyer, testSeller)));
    }

    @Test
    @WithMockUser(username = "chat_buyer@kln.ac.lk")
    void startOrGetConversation_Authenticated_Returns201Created() throws Exception {
        ConversationRequestDto request = new ConversationRequestDto(testListing.getId());

        mockMvc.perform(post("/api/v1/chat/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.listingId").value(testListing.getId()))
                .andExpect(jsonPath("$.buyerName").value("Chat Buyer"));
    }

    @Test
    @WithMockUser(username = "chat_buyer@kln.ac.lk")
    void getUserConversations_Authenticated_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/chat/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "chat_buyer@kln.ac.lk")
    void sendMessageAndFetchMessages_Authenticated_Success() throws Exception {
        MessageRequestDto messageRequest = new MessageRequestDto("Hi seller, is this item available?");

        mockMvc.perform(post("/api/v1/chat/conversations/" + testConversation.getId() + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.messageText").value("Hi seller, is this item available?"));

        mockMvc.perform(get("/api/v1/chat/conversations/" + testConversation.getId() + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "chat_seller@kln.ac.lk")
    void markAsRead_Authenticated_Returns204NoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/chat/conversations/" + testConversation.getId() + "/read"))
                .andExpect(status().isNoContent());
    }
}
