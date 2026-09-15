package varna.mit.kln.unimart.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;

public class OrderResponseDto {

    private Integer id;
    private Integer listingId;
    private String listingTitle;
    private Integer buyerId;
    private String buyerName;
    private Integer sellerId;
    private String sellerName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponseDto() {}

    public OrderResponseDto(Order order) {
        this.id = order.getId();
        if (order.getListing() != null) {
            this.listingId = order.getListing().getId();
            this.listingTitle = order.getListing().getTitle();
            if (order.getListing().getSeller() != null) {
                this.sellerId = order.getListing().getSeller().getId();
                this.sellerName = order.getListing().getSeller().getFullName();
            }
        }
        if (order.getBuyer() != null) {
            this.buyerId = order.getBuyer().getId();
            this.buyerName = order.getBuyer().getFullName();
        }
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.paymentMethod = order.getPaymentMethod();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
