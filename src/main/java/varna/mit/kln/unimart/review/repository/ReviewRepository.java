package varna.mit.kln.unimart.review.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.review.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findByOrderId(Integer orderId);

    Page<Review> findByRevieweeIdOrderByCreatedAtDesc(Integer revieweeId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :sellerId")
    Double findAverageRatingByRevieweeId(@Param("sellerId") Integer sellerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewee.id = :sellerId")
    Long countByRevieweeId(@Param("sellerId") Integer sellerId);
}
