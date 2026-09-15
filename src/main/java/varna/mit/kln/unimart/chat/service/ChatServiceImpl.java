package varna.mit.kln.unimart.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;
import varna.mit.kln.unimart.listing.entity.Listing;
import varna.mit.kln.unimart.listing.entity.ListingStatus;
import varna.mit.kln.unimart.listing.repository.ListingRepository;

@Service
public class ChatServiceImpl implements ChatService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public ChatServiceImpl(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            ListingRepository listingRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    @Override
    @Transactional
    public ConversationResponseDto startOrGetConversation(ConversationRequestDto requestDto, String userEmail) {
        User buyer = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Listing listing = listingRepository.findByIdAndStatusNot(requestDto.getListingId(), ListingStatus.inactive)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with ID: " + requestDto.getListingId()));

        if (listing.getSeller() != null && listing.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("You cannot start a conversation on your own listing");
        }

        Optional<Conversation> existingConv = conversationRepository.findByListingIdAndBuyerId(listing.getId(), buyer.getId());
        Conversation conversation;
        if (existingConv.isPresent()) {
            conversation = existingConv.get();
        } else {
            conversation = new Conversation(listing, buyer, listing.getSeller());
            conversation = conversationRepository.save(conversation);
        }

        Message lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId()).orElse(null);
        long unreadCount = messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNull(conversation.getId(), buyer.getId());

        return new ConversationResponseDto(conversation, lastMessage, unreadCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponseDto> getUserConversations(String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Conversation> conversations = conversationRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(user.getId(), user.getId());

        return conversations.stream().map(conv -> {
            Message lastMessage = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conv.getId()).orElse(null);
            long unreadCount = messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNull(conv.getId(), user.getId());
            return new ConversationResponseDto(conv, lastMessage, unreadCount);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDto> getConversationMessages(Integer conversationId, Pageable pageable, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Conversation conversation = conversationRepository.findByIdAndParticipant(conversationId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a participant in this conversation"));

        int effectivePageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable cappedPageable = PageRequest.of(pageable.getPageNumber(), effectivePageSize, pageable.getSort());

        Page<Message> messagesPage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), cappedPageable);
        return messagesPage.map(MessageResponseDto::new);
    }

    @Override
    @Transactional
    public MessageResponseDto sendMessage(Integer conversationId, MessageRequestDto requestDto, String userEmail) {
        User sender = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Conversation conversation = conversationRepository.findByIdAndParticipant(conversationId, sender.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a participant in this conversation"));

        Message message = new Message(conversation, sender, requestDto.getMessageText());
        Message savedMessage = messageRepository.save(message);

        return new MessageResponseDto(savedMessage);
    }

    @Override
    @Transactional
    public void markAsRead(Integer conversationId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Conversation conversation = conversationRepository.findByIdAndParticipant(conversationId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a participant in this conversation"));

        List<Message> unreadMessages = messageRepository.findByConversationIdAndSenderIdNotAndReadAtIsNull(conversation.getId(), user.getId());
        LocalDateTime now = LocalDateTime.now();
        for (Message msg : unreadMessages) {
            msg.setReadAt(now);
        }
        messageRepository.saveAll(unreadMessages);
    }
}
