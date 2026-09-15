package varna.mit.kln.unimart.notification.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;
import varna.mit.kln.unimart.notification.dto.NotificationResponseDto;
import varna.mit.kln.unimart.notification.dto.UnreadCountResponseDto;
import varna.mit.kln.unimart.notification.entity.Notification;
import varna.mit.kln.unimart.notification.repository.NotificationRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void sendNotification(User recipient, String type, String title, String body) {
        if (recipient == null) {
            return;
        }
        Notification notification = new Notification(recipient, type, title, body);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getUserNotifications(String userEmail, Pageable pageable) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        int effectivePageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable cappedPageable = PageRequest.of(pageable.getPageNumber(), effectivePageSize, pageable.getSort());

        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), cappedPageable);
        return page.map(NotificationResponseDto::new);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponseDto getUnreadCount(String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        long count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return new UnreadCountResponseDto(count);
    }

    @Override
    @Transactional
    public NotificationResponseDto markAsRead(Integer id, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to access this notification");
        }

        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return new NotificationResponseDto(updatedNotification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Notification> unreadList = notificationRepository.findByUserIdAndIsReadFalse(user.getId());
        for (Notification notification : unreadList) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadList);
    }
}
