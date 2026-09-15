package varna.mit.kln.unimart.review.dto;

public class SellerRatingSummaryDto {

    private Integer sellerId;
    private String sellerName;
    private Double averageRating;
    private Long totalReviews;

    public SellerRatingSummaryDto() {}

    public SellerRatingSummaryDto(Integer sellerId, String sellerName, Double averageRating, Long totalReviews) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.averageRating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
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

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
