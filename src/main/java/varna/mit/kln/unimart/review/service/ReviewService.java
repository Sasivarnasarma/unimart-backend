package varna.mit.kln.unimart.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import varna.mit.kln.unimart.review.dto.ReviewRequestDto;
import varna.mit.kln.unimart.review.dto.ReviewResponseDto;
import varna.mit.kln.unimart.review.dto.ReviewUpdateDto;
import varna.mit.kln.unimart.review.dto.SellerRatingSummaryDto;

public interface ReviewService {
    ReviewResponseDto createReview(ReviewRequestDto requestDto, String userEmail);
    Page<ReviewResponseDto> getSellerReviews(Integer sellerId, Pageable pageable);
    SellerRatingSummaryDto getSellerRatingSummary(Integer sellerId);
    ReviewResponseDto updateReview(Integer id, ReviewUpdateDto updateDto, String userEmail);
    void deleteReview(Integer id, String userEmail);
}
