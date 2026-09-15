package varna.mit.kln.unimart.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.common.exception.ConflictException;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.order.entity.Order;
import varna.mit.kln.unimart.order.entity.OrderStatus;
import varna.mit.kln.unimart.order.repository.OrderRepository;
import varna.mit.kln.unimart.review.dto.ReviewRequestDto;
import varna.mit.kln.unimart.review.dto.ReviewResponseDto;
import varna.mit.kln.unimart.review.dto.ReviewUpdateDto;
import varna.mit.kln.unimart.review.dto.SellerRatingSummaryDto;
import varna.mit.kln.unimart.review.entity.Review;
import varna.mit.kln.unimart.review.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private ReviewService reviewService;

    private User buyer;
    private User seller;
    private Order order;
    private Listing listing;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, orderRepository, userRepository);

        buyer = new User();
        buyer.setId(1);
        buyer.setUniversityEmail("rev_buyer@kln.ac.lk");
        buyer.setFullName("Review Buyer");

        seller = new User();
        seller.setId(2);
        seller.setUniversityEmail("rev_seller@kln.ac.lk");
        seller.setFullName("Review Seller");

        listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);
        listing.setTitle("Textbook");

        order = new Order();
        order.setId(100);
        order.setBuyer(buyer);
        order.setListing(listing);
        order.setStatus(OrderStatus.completed);
    }

    @Test
    void createReview_Success() {
        ReviewRequestDto request = new ReviewRequestDto(100, 5, "Great seller!");

        when(userRepository.findByUniversityEmail("rev_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(100)).thenReturn(Optional.of(order));
        when(reviewRepository.findByOrderId(100)).thenReturn(Optional.empty());

        Review savedReview = new Review(order, buyer, seller, 5, "Great seller!");
        savedReview.setId(300);

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponseDto response = reviewService.createReview(request, "rev_buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(300, response.getId());
        assertEquals(100, response.getOrderId());
        assertEquals(1, response.getReviewerId());
        assertEquals(2, response.getRevieweeId());
        assertEquals(5, response.getRating());
        assertEquals("Great seller!", response.getComment());
    }

    @Test
    void createReview_OrderNotCompleted_ThrowsConflictException() {
        order.setStatus(OrderStatus.pending);
        ReviewRequestDto request = new ReviewRequestDto(100, 5, "Great seller!");

        when(userRepository.findByUniversityEmail("rev_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(100)).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> reviewService.createReview(request, "rev_buyer@kln.ac.lk"));
    }

    @Test
    void createReview_NonBuyer_ThrowsAccessDeniedException() {
        ReviewRequestDto request = new ReviewRequestDto(100, 5, "Great seller!");

        User unrelatedUser = new User();
        unrelatedUser.setId(99);
        unrelatedUser.setUniversityEmail("other@kln.ac.lk");

        when(userRepository.findByUniversityEmail("other@kln.ac.lk")).thenReturn(Optional.of(unrelatedUser));
        when(orderRepository.findById(100)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> reviewService.createReview(request, "other@kln.ac.lk"));
    }

    @Test
    void createReview_DuplicateReview_ThrowsConflictException() {
        ReviewRequestDto request = new ReviewRequestDto(100, 5, "Great seller!");

        Review existingReview = new Review(order, buyer, seller, 5, "Previous review");
        existingReview.setId(300);

        when(userRepository.findByUniversityEmail("rev_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(100)).thenReturn(Optional.of(order));
        when(reviewRepository.findByOrderId(100)).thenReturn(Optional.of(existingReview));

        assertThrows(ConflictException.class, () -> reviewService.createReview(request, "rev_buyer@kln.ac.lk"));
    }

    @Test
    void getSellerReviews_Success() {
        Review review = new Review(order, buyer, seller, 5, "Excellent item!");
        review.setId(300);

        Page<Review> page = new PageImpl<>(List.of(review));

        when(userRepository.findById(2)).thenReturn(Optional.of(seller));
        when(reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(eq(2), any(Pageable.class))).thenReturn(page);

        Page<ReviewResponseDto> result = reviewService.getSellerReviews(2, PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Excellent item!", result.getContent().get(0).getComment());
    }

    @Test
    void getSellerRatingSummary_Success() {
        when(userRepository.findById(2)).thenReturn(Optional.of(seller));
        when(reviewRepository.findAverageRatingByRevieweeId(2)).thenReturn(4.67);
        when(reviewRepository.countByRevieweeId(2)).thenReturn(12L);

        SellerRatingSummaryDto summary = reviewService.getSellerRatingSummary(2);

        assertNotNull(summary);
        assertEquals(2, summary.getSellerId());
        assertEquals(4.7, summary.getAverageRating());
        assertEquals(12L, summary.getTotalReviews());
    }

    @Test
    void updateReview_Success_AsAuthor() {
        Review review = new Review(order, buyer, seller, 4, "Good");
        review.setId(300);

        ReviewUpdateDto updateDto = new ReviewUpdateDto(5, "Updated to 5 stars!");

        when(userRepository.findByUniversityEmail("rev_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(reviewRepository.findById(300)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        ReviewResponseDto response = reviewService.updateReview(300, updateDto, "rev_buyer@kln.ac.lk");

        assertEquals(5, response.getRating());
        assertEquals("Updated to 5 stars!", response.getComment());
    }

    @Test
    void updateReview_NonAuthor_ThrowsAccessDeniedException() {
        Review review = new Review(order, buyer, seller, 4, "Good");
        review.setId(300);

        ReviewUpdateDto updateDto = new ReviewUpdateDto(5, "Updated!");

        User otherUser = new User();
        otherUser.setId(99);
        otherUser.setUniversityEmail("other@kln.ac.lk");

        when(userRepository.findByUniversityEmail("other@kln.ac.lk")).thenReturn(Optional.of(otherUser));
        when(reviewRepository.findById(300)).thenReturn(Optional.of(review));

        assertThrows(AccessDeniedException.class, () -> reviewService.updateReview(300, updateDto, "other@kln.ac.lk"));
    }

    @Test
    void deleteReview_Success_AsAuthor() {
        Review review = new Review(order, buyer, seller, 4, "Good");
        review.setId(300);

        when(userRepository.findByUniversityEmail("rev_buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(reviewRepository.findById(300)).thenReturn(Optional.of(review));

        reviewService.deleteReview(300, "rev_buyer@kln.ac.lk");

        verify(reviewRepository, times(1)).delete(review);
    }
}
