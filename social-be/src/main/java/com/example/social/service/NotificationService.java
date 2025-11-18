package com.example.social.service;

import com.example.social.domain.Reaction;
import com.example.social.domain.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void emitPostLikeUpdated(Long postId) {
        messagingTemplate.convertAndSend("/topic/post/" + postId + "/likes", Map.of("postId", postId));
    }

    public void notifyUserPostReacted(User postOwner, Long postId, Long reactorUserId, Reaction reaction) {
        sendToUser(postOwner, "/queue/notifications",
                Map.of("type", "POST_REACTION", "postId", postId, "fromUserId", reactorUserId, "reaction", reaction.name()));
    }

    public void notifyNewMessage(User receiver, Long messageId, Long fromUserId) {
        sendToUser(receiver, "/queue/messages",
                Map.of("type", "NEW_MESSAGE", "messageId", messageId, "fromUserId", fromUserId));
    }

    private void sendToUser(User user, String destination, Object payload) {
        Optional.ofNullable(user)
                .map(User::getEmail)
                .filter(email -> !email.isBlank())
                .ifPresent(email -> messagingTemplate.convertAndSendToUser(email, destination, payload));
    }
}