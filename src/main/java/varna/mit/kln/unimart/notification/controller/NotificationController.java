package varna.mit.kln.unimart.notification.controller;

import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.notification.dto.NotificationResponseDto;
import varna.mit.kln.unimart.notification.dto.UnreadCountResponseDto;
import varna.mit.kln.unimart.notification.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getUserNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        Page<NotificationResponseDto> page = notificationService.getUserNotifications(principal.getName(), pageable);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponseDto> getUnreadCount(Principal principal) {
        UnreadCountResponseDto unreadCount = notificationService.getUnreadCount(principal.getName());
        return new ResponseEntity<>(unreadCount, HttpStatus.OK);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @PathVariable Integer id,
            Principal principal) {
        NotificationResponseDto responseDto = notificationService.markAsRead(id, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
