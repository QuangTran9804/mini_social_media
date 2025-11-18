package com.example.social.controller;

import com.example.social.domain.Message;
import com.example.social.service.CurrentUserService;
import com.example.social.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.social.dto.response.message.MessageResponseDTO;

@RestController
@RequestMapping("/api/messages")
@Validated
public class MessageController {

    private final MessageService messageService;
    private final CurrentUserService currentUserService;

    public MessageController(MessageService messageService, CurrentUserService currentUserService) {
        this.messageService = messageService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponseDTO> send(@RequestBody Map<String, Object> body) throws com.example.social.controller.error.ResourceNotFoundException {
        Long senderId = currentUserService.getCurrentUserId();
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = body.getOrDefault("content", "").toString();
        @SuppressWarnings("unchecked")
        List<Integer> aid = (List<Integer>) body.getOrDefault("attachmentIds", List.of());
        List<Long> attachmentIds = aid.stream().map(Integer::longValue).toList();

        Message message = messageService.sendMessage(senderId, receiverId, content, attachmentIds);
        return ResponseEntity.ok(toDto(message));
    }

    @GetMapping("/with/{otherUserId}")
    public ResponseEntity<List<MessageResponseDTO>> conversation(@PathVariable Long otherUserId) throws com.example.social.controller.error.ResourceNotFoundException {
        Long me = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(messageService.getConversation(me, otherUserId).stream().map(this::toDto).toList());
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long messageId) throws com.example.social.controller.error.ResourceNotFoundException {
        Long me = currentUserService.getCurrentUserId();
        messageService.markAsRead(messageId, me);
        return ResponseEntity.noContent().build();
    }

    private MessageResponseDTO toDto(Message message) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .receiverId(message.getReceiver().getId())
                .content(message.getContent())
                .isRead(Boolean.TRUE.equals(message.getIsRead()))
                .sentAt(message.getSentAt())
                .build();
    }
}


