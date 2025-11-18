package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.*;
import com.example.social.repository.AttachmentRepository;
import com.example.social.repository.MessageRepository;
import com.example.social.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final NotificationService notificationService;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          AttachmentRepository attachmentRepository,
                          NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content, List<Long> attachmentIds) throws com.example.social.controller.error.ResourceNotFoundException {
        User sender = userRepository.findById(senderId).orElseThrow(() -> new com.example.social.controller.error.ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new com.example.social.controller.error.ResourceNotFoundException("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        Message saved = messageRepository.save(message);

        if (attachmentIds != null && !attachmentIds.isEmpty()) {
            List<Attachment> atts = attachmentRepository.findAllById(attachmentIds);
            for (Attachment att : atts) {
                att.setMessage(saved);
                att.setUsedFor(AttachmentUsage.MESSAGE);
            }
            attachmentRepository.saveAll(atts);
        }

        notificationService.notifyNewMessage(receiver, saved.getId(), senderId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Message> getConversation(Long userAId, Long userBId) throws com.example.social.controller.error.ResourceNotFoundException {
        User a = userRepository.findById(userAId).orElseThrow(() -> new com.example.social.controller.error.ResourceNotFoundException("User not found"));
        User b = userRepository.findById(userBId).orElseThrow(() -> new com.example.social.controller.error.ResourceNotFoundException("User not found"));
        return messageRepository.findConversation(a, b);
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) throws com.example.social.controller.error.ResourceNotFoundException {
        Message m = messageRepository.findById(messageId).orElseThrow(() -> new com.example.social.controller.error.ResourceNotFoundException("Message not found"));
        if (m.getReceiver().getId().equals(userId)) {
            m.setIsRead(Boolean.TRUE);
            messageRepository.save(m);
        }
    }
}


