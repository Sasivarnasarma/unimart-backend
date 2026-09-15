package varna.mit.kln.unimart.chat.dto;

import java.time.LocalDateTime;
import varna.mit.kln.unimart.chat.entity.Message;

public class MessageResponseDto {

    private Integer id;
    private Integer conversationId;
    private Integer senderId;
    private String senderName;
    private String messageText;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public MessageResponseDto() {}

    public MessageResponseDto(Message message) {
        this.id = message.getId();
        if (message.getConversation() != null) {
            this.conversationId = message.getConversation().getId();
        }
        if (message.getSender() != null) {
            this.senderId = message.getSender().getId();
            this.senderName = message.getSender().getFullName();
        }
        this.messageText = message.getMessageText();
        this.readAt = message.getReadAt();
        this.createdAt = message.getCreatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
