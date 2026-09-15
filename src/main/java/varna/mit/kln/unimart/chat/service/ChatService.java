package varna.mit.kln.unimart.chat.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import varna.mit.kln.unimart.chat.dto.ConversationRequestDto;
import varna.mit.kln.unimart.chat.dto.ConversationResponseDto;
import varna.mit.kln.unimart.chat.dto.MessageRequestDto;
import varna.mit.kln.unimart.chat.dto.MessageResponseDto;

public interface ChatService {
    ConversationResponseDto startOrGetConversation(ConversationRequestDto requestDto, String userEmail);
    List<ConversationResponseDto> getUserConversations(String userEmail);
    Page<MessageResponseDto> getConversationMessages(Integer conversationId, Pageable pageable, String userEmail);
    MessageResponseDto sendMessage(Integer conversationId, MessageRequestDto requestDto, String userEmail);
    void markAsRead(Integer conversationId, String userEmail);
}
