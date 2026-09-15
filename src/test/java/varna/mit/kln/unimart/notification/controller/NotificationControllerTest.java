package varna.mit.kln.unimart.notification.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.entity.UserRole;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.notification.entity.Notification;
import varna.mit.kln.unimart.notification.repository.NotificationRepository;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class NotificationControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private MockMvc mockMvc;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testUser = userRepository.findByUniversityEmail("notif_ctrl_user@kln.ac.lk")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUniversityEmail("notif_ctrl_user@kln.ac.lk");
                    u.setPasswordHash("pass");
                    u.setFullName("Notif Ctrl User");
                    u.setRole(UserRole.buyer);
                    return userRepository.save(u);
                });

        testNotification = notificationRepository.save(new Notification(testUser, "ORDER_STATUS", "Test Alert", "Alert body text"));
    }

    @Test
    @WithMockUser(username = "notif_ctrl_user@kln.ac.lk")
    void getUserNotifications_Authenticated_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "notif_ctrl_user@kln.ac.lk")
    void getUnreadCount_Authenticated_Returns200OK() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").exists());
    }

    @Test
    @WithMockUser(username = "notif_ctrl_user@kln.ac.lk")
    void markAsRead_Authenticated_Returns200OK() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/" + testNotification.getId() + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testNotification.getId()))
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    @WithMockUser(username = "notif_ctrl_user@kln.ac.lk")
    void markAllAsRead_Authenticated_Returns204NoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/read-all"))
                .andExpect(status().isNoContent());
    }
}
