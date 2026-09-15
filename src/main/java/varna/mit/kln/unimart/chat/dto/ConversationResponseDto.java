package varna.mit.kln.unimart.chat.dto;

import java.time.LocalDateTime;
import varna.mit.kln.unimart.chat.entity.Conversation;
import varna.mit.kln.unimart.chat.entity.Message;

public class ConversationResponseDto {

    private Integer id;
    private Integer listingId;
    private String listingTitle;
    private Integer buyerId;
    private String buyerName;
    private Integer sellerId;
    private String sellerName;
    private String lastMessageText;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
    private LocalDateTime createdAt;

    public ConversationResponseDto() {}

    public ConversationResponseDto(Conversation conversation) {
        this(conversation, null, 0);
    }

    public ConversationResponseDto(Conversation conversation, Message lastMessage, long unreadCount) {
        this.id = conversation.getId();
        if (conversation.getListing() != null) {
            this.listingId = conversation.getListing().getId();
            this.listingTitle = conversation.getListing().getTitle();
        }
        if (conversation.getBuyer() != null) {
            this.buyerId = conversation.getBuyer().getId();
            this.buyerName = conversation.getBuyer().getFullName();
        }
        if (conversation.getSeller() != null) {
            this.sellerId = conversation.getSeller().getId();
            this.sellerName = conversation.getSeller().getFullName();
        }
        if (lastMessage != null) {
            this.lastMessageText = lastMessage.getMessageText();
            this.lastMessageTime = lastMessage.getCreatedAt();
        }
        this.unreadCount = unreadCount;
        this.createdAt = conversation.getCreatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getListingId() {
        return listingId;
    }

    public void setListingId(Integer listingId) {
        this.listingId = listingId;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
