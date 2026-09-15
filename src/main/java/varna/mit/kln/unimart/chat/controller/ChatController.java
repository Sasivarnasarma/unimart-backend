package varna.mit.kln.unimart.chat.controller;

import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.chat.dto.ConversationRequestDto;
import varna.mit.kln.unimart.chat.dto.ConversationResponseDto;
import varna.mit.kln.unimart.chat.dto.MessageRequestDto;
import varna.mit.kln.unimart.chat.dto.MessageResponseDto;
import varna.mit.kln.unimart.chat.service.ChatService;

@RestController
@RequestMapping("/api/v1/chat/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ConversationResponseDto> startOrGetConversation(
            @Valid @RequestBody ConversationRequestDto requestDto,
            Principal principal) {
        ConversationResponseDto responseDto = chatService.startOrGetConversation(requestDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponseDto>> getUserConversations(Principal principal) {
        List<ConversationResponseDto> conversations = chatService.getUserConversations(principal.getName());
        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageResponseDto>> getConversationMessages(
            @PathVariable Integer id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        Page<MessageResponseDto> messages = chatService.getConversationMessages(id, pageable, principal.getName());
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @PathVariable Integer id,
            @Valid @RequestBody MessageRequestDto requestDto,
            Principal principal) {
        MessageResponseDto responseDto = chatService.sendMessage(id, requestDto, principal.getName());
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Integer id,
            Principal principal) {
        chatService.markAsRead(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
