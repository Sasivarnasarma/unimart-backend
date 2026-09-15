package varna.mit.kln.unimart.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.notification.dto.NotificationResponseDto;
import varna.mit.kln.unimart.notification.dto.UnreadCountResponseDto;

public interface NotificationService {
    void sendNotification(User recipient, String type, String title, String body);
    Page<NotificationResponseDto> getUserNotifications(String userEmail, Pageable pageable);
    UnreadCountResponseDto getUnreadCount(String userEmail);
    NotificationResponseDto markAsRead(Integer id, String userEmail);
    void markAllAsRead(String userEmail);
}
