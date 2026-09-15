package varna.mit.kln.unimart.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.chat.dto.ConversationRequestDto;
import varna.mit.kln.unimart.chat.dto.ConversationResponseDto;
import varna.mit.kln.unimart.chat.dto.MessageRequestDto;
import varna.mit.kln.unimart.chat.dto.MessageResponseDto;
import varna.mit.kln.unimart.chat.entity.Conversation;
import varna.mit.kln.unimart.chat.entity.Message;
import varna.mit.kln.unimart.chat.repository.ConversationRepository;
import varna.mit.kln.unimart.chat.repository.MessageRepository;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingStatus;
import varna.mit.kln.unimart.listing.repository.ListingRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    private ChatService chatService;

    private User buyer;
    private User seller;
    private Listing listing;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(conversationRepository, messageRepository, userRepository, listingRepository);

        buyer = new User();
        buyer.setId(1);
        buyer.setUniversityEmail("buyer@kln.ac.lk");
        buyer.setFullName("Buyer Student");

        seller = new User();
        seller.setId(2);
        seller.setUniversityEmail("seller@kln.ac.lk");
        seller.setFullName("Seller Student");

        listing = new Listing();
        listing.setId(10);
        listing.setSeller(seller);
        listing.setTitle("Course Textbook");
    }

    @Test
    void startOrGetConversation_Success() {
        ConversationRequestDto request = new ConversationRequestDto(10);

        when(userRepository.findByUniversityEmail("buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(listingRepository.findByIdAndStatusNot(10, ListingStatus.inactive)).thenReturn(Optional.of(listing));
        when(conversationRepository.findByListingIdAndBuyerId(10, 1)).thenReturn(Optional.empty());

        Conversation savedConv = new Conversation(listing, buyer, seller);
        savedConv.setId(100);

        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConv);
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(100)).thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNull(100, 1)).thenReturn(0L);

        ConversationResponseDto response = chatService.startOrGetConversation(request, "buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(100, response.getId());
        assertEquals(10, response.getListingId());
        assertEquals("Course Textbook", response.getListingTitle());
        assertEquals(1, response.getBuyerId());
        assertEquals(2, response.getSellerId());
    }

    @Test
    void startOrGetConversation_ReturnsExisting_IfPresent() {
        ConversationRequestDto request = new ConversationRequestDto(10);

        Conversation existingConv = new Conversation(listing, buyer, seller);
        existingConv.setId(100);

        when(userRepository.findByUniversityEmail("buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(listingRepository.findByIdAndStatusNot(10, ListingStatus.inactive)).thenReturn(Optional.of(listing));
        when(conversationRepository.findByListingIdAndBuyerId(10, 1)).thenReturn(Optional.of(existingConv));
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(100)).thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNull(100, 1)).thenReturn(0L);

        ConversationResponseDto response = chatService.startOrGetConversation(request, "buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(100, response.getId());
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void startOrGetConversation_SelfChat_ThrowsIllegalArgumentException() {
        ConversationRequestDto request = new ConversationRequestDto(10);

        when(userRepository.findByUniversityEmail("seller@kln.ac.lk")).thenReturn(Optional.of(seller));
        when(listingRepository.findByIdAndStatusNot(10, ListingStatus.inactive)).thenReturn(Optional.of(listing));

        assertThrows(IllegalArgumentException.class, () -> chatService.startOrGetConversation(request, "seller@kln.ac.lk"));
    }

    @Test
    void getUserConversations_Success() {
        Conversation conv = new Conversation(listing, buyer, seller);
        conv.setId(100);

        when(userRepository.findByUniversityEmail("buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(conversationRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(1, 1)).thenReturn(List.of(conv));

        List<ConversationResponseDto> conversations = chatService.getUserConversations("buyer@kln.ac.lk");

        assertEquals(1, conversations.size());
        assertEquals(100, conversations.get(0).getId());
    }

    @Test
    void getConversationMessages_Success_AsParticipant() {
        Conversation conv = new Conversation(listing, buyer, seller);
        conv.setId(100);

        Message msg = new Message(conv, buyer, "Hello!");
        msg.setId(500);

        Page<Message> page = new PageImpl<>(List.of(msg));

        when(userRepository.findByUniversityEmail("buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(conversationRepository.findByIdAndParticipant(100, 1)).thenReturn(Optional.of(conv));
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq(100), any(Pageable.class))).thenReturn(page);

        Page<MessageResponseDto> result = chatService.getConversationMessages(100, PageRequest.of(0, 20), "buyer@kln.ac.lk");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Hello!", result.getContent().get(0).getMessageText());
    }

    @Test
    void getConversationMessages_Unauthorized_ThrowsAccessDeniedException() {
        when(userRepository.findByUniversityEmail("other@kln.ac.lk")).thenReturn(Optional.of(new User()));
        when(conversationRepository.findByIdAndParticipant(eq(100), any())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> chatService.getConversationMessages(100, PageRequest.of(0, 20), "other@kln.ac.lk"));
    }

    @Test
    void sendMessage_Success_AsParticipant() {
        Conversation conv = new Conversation(listing, buyer, seller);
        conv.setId(100);

        MessageRequestDto request = new MessageRequestDto("Is this still available?");

        Message savedMsg = new Message(conv, buyer, "Is this still available?");
        savedMsg.setId(501);

        when(userRepository.findByUniversityEmail("buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(conversationRepository.findByIdAndParticipant(100, 1)).thenReturn(Optional.of(conv));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMsg);

        MessageResponseDto response = chatService.sendMessage(100, request, "buyer@kln.ac.lk");

        assertNotNull(response);
        assertEquals(501, response.getId());
        assertEquals("Is this still available?", response.getMessageText());
    }

    @Test
    void sendMessage_Unauthorized_ThrowsAccessDeniedException() {
        MessageRequestDto request = new MessageRequestDto("Hello!");

        when(userRepository.findByUniversityEmail("other@kln.ac.lk")).thenReturn(Optional.of(new User()));
        when(conversationRepository.findByIdAndParticipant(eq(100), any())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> chatService.sendMessage(100, request, "other@kln.ac.lk"));
    }

    @Test
    void markAsRead_Success() {
        Conversation conv = new Conversation(listing, buyer, seller);
        conv.setId(100);

        Message unreadMsg = new Message(conv, seller, "Yes it is available!");
        unreadMsg.setId(502);

        when(userRepository.findByUniversityEmail("buyer@kln.ac.lk")).thenReturn(Optional.of(buyer));
        when(conversationRepository.findByIdAndParticipant(100, 1)).thenReturn(Optional.of(conv));
        when(messageRepository.findByConversationIdAndSenderIdNotAndReadAtIsNull(100, 1)).thenReturn(List.of(unreadMsg));

        chatService.markAsRead(100, "buyer@kln.ac.lk");

        assertNotNull(unreadMsg.getReadAt());
        verify(messageRepository, times(1)).saveAll(anyList());
    }
}
