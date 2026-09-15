package varna.mit.kln.unimart.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.notification.dto.NotificationResponseDto;
import varna.mit.kln.unimart.notification.dto.UnreadCountResponseDto;
import varna.mit.kln.unimart.notification.entity.Notification;
import varna.mit.kln.unimart.notification.repository.NotificationRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    private User recipient;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository, userRepository);

        recipient = new User();
        recipient.setId(1);
        recipient.setUniversityEmail("notif_user@kln.ac.lk");
        recipient.setFullName("Notification User");
    }

    @Test
    void sendNotification_Success() {
        notificationService.sendNotification(recipient, "ORDER_STATUS", "Order Placed", "Your order was placed successfully.");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void sendNotification_NullRecipient_DoesNotSave() {
        notificationService.sendNotification(null, "ORDER_STATUS", "Order Placed", "Body");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_Success() {
        Notification notif = new Notification(recipient, "ORDER_STATUS", "Order Paid", "Payment received.");
        notif.setId(400);

        Page<Notification> page = new PageImpl<>(List.of(notif));

        when(userRepository.findByUniversityEmail("notif_user@kln.ac.lk")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1), any(Pageable.class))).thenReturn(page);

        Page<NotificationResponseDto> result = notificationService.getUserNotifications("notif_user@kln.ac.lk", PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Order Paid", result.getContent().get(0).getTitle());
    }

    @Test
    void getUnreadCount_Success() {
        when(userRepository.findByUniversityEmail("notif_user@kln.ac.lk")).thenReturn(Optional.of(recipient));
        when(notificationRepository.countByUserIdAndIsReadFalse(1)).thenReturn(3L);

        UnreadCountResponseDto response = notificationService.getUnreadCount("notif_user@kln.ac.lk");

        assertNotNull(response);
        assertEquals(3L, response.getUnreadCount());
    }

    @Test
    void markAsRead_Success_AsOwner() {
        Notification notif = new Notification(recipient, "ORDER_STATUS", "Order Delivered", "Package ready.");
        notif.setId(400);

        when(userRepository.findByUniversityEmail("notif_user@kln.ac.lk")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(400)).thenReturn(Optional.of(notif));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResponseDto response = notificationService.markAsRead(400, "notif_user@kln.ac.lk");

        assertTrue(response.getIsRead());
    }

    @Test
    void markAsRead_NonOwner_ThrowsAccessDeniedException() {
        Notification notif = new Notification(recipient, "ORDER_STATUS", "Order Delivered", "Package ready.");
        notif.setId(400);

        User otherUser = new User();
        otherUser.setId(99);
        otherUser.setUniversityEmail("other@kln.ac.lk");

        when(userRepository.findByUniversityEmail("other@kln.ac.lk")).thenReturn(Optional.of(otherUser));
        when(notificationRepository.findById(400)).thenReturn(Optional.of(notif));

        assertThrows(AccessDeniedException.class, () -> notificationService.markAsRead(400, "other@kln.ac.lk"));
    }

    @Test
    void markAllAsRead_Success() {
        Notification unread1 = new Notification(recipient, "ORDER_STATUS", "Title 1", "Body 1");
        Notification unread2 = new Notification(recipient, "NEW_MESSAGE", "Title 2", "Body 2");

        when(userRepository.findByUniversityEmail("notif_user@kln.ac.lk")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByUserIdAndIsReadFalse(1)).thenReturn(List.of(unread1, unread2));

        notificationService.markAllAsRead("notif_user@kln.ac.lk");

        assertTrue(unread1.getIsRead());
        assertTrue(unread2.getIsRead());
        verify(notificationRepository, times(1)).saveAll(anyList());
    }
}
