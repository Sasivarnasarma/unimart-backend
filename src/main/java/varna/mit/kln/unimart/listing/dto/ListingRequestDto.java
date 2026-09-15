package varna.mit.kln.unimart.listing.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import varna.mit.kln.unimart.listing.entity.ListingStatus;

public class ListingRequestDto {

    private Integer sellerId;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    private ListingStatus status = ListingStatus.available;

    @Valid
    private List<ListingImageRequestDto> images;

    public ListingRequestDto() {
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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

    public List<ListingImageRequestDto> getImages() {
        return images;
    }

    public void setImages(List<ListingImageRequestDto> images) {
        this.images = images;
    }
}
