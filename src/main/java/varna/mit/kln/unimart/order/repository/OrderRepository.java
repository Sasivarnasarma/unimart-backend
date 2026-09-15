package varna.mit.kln.unimart.order.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.order.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByBuyerIdOrListingSellerIdOrderByCreatedAtDesc(Integer buyerId, Integer sellerId);

    @Query("SELECT o FROM Order o WHERE o.id = :id AND (o.buyer.id = :userId OR o.listing.seller.id = :userId)")
    Optional<Order> findByIdAndParticipant(@Param("id") Integer id, @Param("userId") Integer userId);
}
