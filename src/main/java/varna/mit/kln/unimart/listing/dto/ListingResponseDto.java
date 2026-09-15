package varna.mit.kln.unimart.listing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingStatus;

public class ListingResponseDto {

    private Integer id;
    private Integer sellerId;
    private String sellerName;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private ListingStatus status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ListingResponseDto() {
    }

    public ListingResponseDto(Listing listing) {
        this.id = listing.getId();
        if (listing.getSeller() != null) {
            this.sellerId = listing.getSeller().getId();
            this.sellerName = listing.getSeller().getFullName();
        }
        if (listing.getCategory() != null) {
            this.categoryId = listing.getCategory().getId();
            this.categoryName = listing.getCategory().getName();
        }
        this.title = listing.getTitle();
        this.description = listing.getDescription();
        this.price = listing.getPrice();
        this.status = listing.getStatus();
        this.version = listing.getVersion();
        this.createdAt = listing.getCreatedAt();
        this.updatedAt = listing.getUpdatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
