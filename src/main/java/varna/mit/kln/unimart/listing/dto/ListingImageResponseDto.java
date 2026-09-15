package varna.mit.kln.unimart.listing.dto;

import varna.mit.kln.unimart.listing.entity.ListingImage;

public class ListingImageResponseDto {

    private Integer id;
    private Integer listingId;
    private String imageUrl;
    private Integer sortOrder;

    public ListingImageResponseDto() {}

    public ListingImageResponseDto(ListingImage image) {
        this.id = image.getId();
        if (image.getListing() != null) {
            this.listingId = image.getListing().getId();
        }
        this.imageUrl = image.getImageUrl();
        this.sortOrder = image.getSortOrder();
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
