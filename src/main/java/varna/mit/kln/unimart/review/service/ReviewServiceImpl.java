package varna.mit.kln.unimart.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.common.exception.ConflictException;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;
import varna.mit.kln.unimart.order.repository.OrderRepository;
import varna.mit.kln.unimart.review.dto.ReviewRequestDto;
import varna.mit.kln.unimart.review.dto.ReviewResponseDto;
import varna.mit.kln.unimart.review.dto.ReviewUpdateDto;
import varna.mit.kln.unimart.review.dto.SellerRatingSummaryDto;
import varna.mit.kln.unimart.review.entity.Review;
import varna.mit.kln.unimart.review.repository.ReviewRepository;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto requestDto, String userEmail) {
        User reviewer = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + requestDto.getOrderId()));

        if (order.getStatus() != OrderStatus.completed) {
            throw new ConflictException("Review can only be submitted for completed orders");
        }

        if (!order.getBuyer().getId().equals(reviewer.getId())) {
            throw new AccessDeniedException("Only the buyer of the order can post a review");
        }

        if (reviewRepository.findByOrderId(order.getId()).isPresent()) {
            throw new ConflictException("A review has already been submitted for this order");
        }

        User reviewee = order.getListing() != null ? order.getListing().getSeller() : null;
        if (reviewee == null) {
            throw new IllegalStateException("Seller details not found for order listing");
        }

        Review review = new Review(
                order,
                reviewer,
                reviewee,
                requestDto.getRating(),
                requestDto.getComment()
        );

        Review savedReview = reviewRepository.save(review);
        return new ReviewResponseDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getSellerReviews(Integer sellerId, Pageable pageable) {
        userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with ID: " + sellerId));

        int effectivePageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable cappedPageable = PageRequest.of(pageable.getPageNumber(), effectivePageSize, pageable.getSort());

        Page<Review> reviewsPage = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(sellerId, cappedPageable);
        return reviewsPage.map(ReviewResponseDto::new);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerRatingSummaryDto getSellerRatingSummary(Integer sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with ID: " + sellerId));

        Double avgRating = reviewRepository.findAverageRatingByRevieweeId(sellerId);
        Long count = reviewRepository.countByRevieweeId(sellerId);

        return new SellerRatingSummaryDto(seller.getId(), seller.getFullName(), avgRating, count);
    }

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Integer id, ReviewUpdateDto updateDto, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        if (!review.getReviewer().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this review");
        }

        if (updateDto.getRating() != null) {
            review.setRating(updateDto.getRating());
        }
        if (updateDto.getComment() != null) {
            review.setComment(updateDto.getComment());
        }

        Review updatedReview = reviewRepository.save(review);
        return new ReviewResponseDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Integer id, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        if (!review.getReviewer().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }
}
