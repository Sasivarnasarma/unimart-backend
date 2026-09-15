package varna.mit.kln.unimart.review.dto;

import java.time.LocalDateTime;
import varna.mit.kln.unimart.review.entity.Review;

public class ReviewResponseDto {

    private Integer id;
    private Integer orderId;
    private Integer reviewerId;
    private String reviewerName;
    private Integer revieweeId;
    private String revieweeName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponseDto() {}

    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        if (review.getOrder() != null) {
            this.orderId = review.getOrder().getId();
        }
        if (review.getReviewer() != null) {
            this.reviewerId = review.getReviewer().getId();
            this.reviewerName = review.getReviewer().getFullName();
        }
        if (review.getReviewee() != null) {
            this.revieweeId = review.getReviewee().getId();
            this.revieweeName = review.getReviewee().getFullName();
        }
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Integer getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(Integer revieweeId) {
        this.revieweeId = revieweeId;
    }

    public String getRevieweeName() {
        return revieweeName;
    }

    public void setRevieweeName(String revieweeName) {
        this.revieweeName = revieweeName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
