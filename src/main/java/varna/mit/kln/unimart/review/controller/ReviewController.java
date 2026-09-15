package varna.mit.kln.unimart.review.controller;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.review.dto.ReviewRequestDto;
import varna.mit.kln.unimart.review.dto.ReviewResponseDto;
import varna.mit.kln.unimart.review.dto.ReviewUpdateDto;
import varna.mit.kln.unimart.review.dto.SellerRatingSummaryDto;
import varna.mit.kln.unimart.review.service.ReviewService;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto requestDto,
            Principal principal) {
        ReviewResponseDto responseDto = reviewService.createReview(requestDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<ReviewResponseDto>> getSellerReviews(
            @PathVariable Integer sellerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponseDto> reviews = reviewService.getSellerReviews(sellerId, pageable);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping("/seller/{sellerId}/summary")
    public ResponseEntity<SellerRatingSummaryDto> getSellerRatingSummary(
            @PathVariable Integer sellerId) {
        SellerRatingSummaryDto summary = reviewService.getSellerRatingSummary(sellerId);
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Integer id,
            @Valid @RequestBody ReviewUpdateDto updateDto,
            Principal principal) {
        ReviewResponseDto responseDto = reviewService.updateReview(id, updateDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer id,
            Principal principal) {
        reviewService.deleteReview(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
