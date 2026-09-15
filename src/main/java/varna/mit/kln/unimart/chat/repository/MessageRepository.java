package varna.mit.kln.unimart.chat.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import varna.mit.kln.unimart.chat.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(Integer conversationId, Pageable pageable);

    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Integer conversationId);

    long countByConversationIdAndSenderIdNotAndReadAtIsNull(Integer conversationId, Integer senderId);

    List<Message> findByConversationIdAndSenderIdNotAndReadAtIsNull(Integer conversationId, Integer senderId);
}
