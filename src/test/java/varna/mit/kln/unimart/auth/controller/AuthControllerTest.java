package varna.mit.kln.unimart.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import varna.mit.kln.unimart.auth.dto.LoginRequestDto;
import varna.mit.kln.unimart.auth.dto.UserRequestDto;
import varna.mit.kln.unimart.auth.entity.UserRole;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void registerUser_ValidPayload_Returns201Created() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setUniversityEmail("testuser" + System.currentTimeMillis() + "@kln.ac.lk");
        request.setPassword("Password123!");
        request.setFullName("Test User");
        request.setRole(UserRole.buyer);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.universityEmail").value(request.getUniversityEmail()))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void registerUser_InvalidEmail_Returns400BadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto();
        request.setUniversityEmail("not-an-email");
        request.setPassword("Password123!");
        request.setFullName("Test User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.universityEmail").exists());
    }

    @Test
    void loginUser_BadCredentials_Returns401Unauthorized() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUniversityEmail("nonexistent@kln.ac.lk");
        request.setPassword("WrongPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void getProfile_Unauthenticated_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
