package varna.mit.kln.unimart.notification.dto;

import java.time.LocalDateTime;
import varna.mit.kln.unimart.notification.entity.Notification;

public class NotificationResponseDto {

    private Integer id;
    private Integer userId;
    private String type;
    private String title;
    private String body;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponseDto() {}

    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        if (notification.getUser() != null) {
            this.userId = notification.getUser().getId();
        }
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
