package varna.mit.kln.unimart.listing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ListingImageRequestDto {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotNull(message = "Sort order is required")
    @Positive(message = "Sort order must be greater than 0")
    private Integer sortOrder = 1;

    public ListingImageRequestDto() {}

    public ListingImageRequestDto(String imageUrl, Integer sortOrder) {
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
