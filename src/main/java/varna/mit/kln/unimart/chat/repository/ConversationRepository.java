package varna.mit.kln.unimart.chat.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.chat.entity.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    List<Conversation> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Integer buyerId, Integer sellerId);

    Optional<Conversation> findByListingIdAndBuyerId(Integer listingId, Integer buyerId);

    @Query("SELECT c FROM Conversation c WHERE c.id = :id AND (c.buyer.id = :userId OR c.seller.id = :userId)")
    Optional<Conversation> findByIdAndParticipant(@Param("id") Integer id, @Param("userId") Integer userId);
}
