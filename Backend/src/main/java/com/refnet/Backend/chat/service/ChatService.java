package com.refnet.Backend.chat.service;

import com.refnet.Backend.chat.dto.ChatMessageDTO;
import com.refnet.Backend.chat.dto.ChatMessageRequest;
import com.refnet.Backend.chat.entity.ChatMessage;
import com.refnet.Backend.chat.repository.ChatMessageRepository;
import com.refnet.Backend.common.exception.AppException;
import com.refnet.Backend.common.exception.ResourceNotFoundException;
import com.refnet.Backend.user.entity.User;
import com.refnet.Backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageDTO saveMessage(User sender, ChatMessageRequest request) {
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new AppException("You cannot send a message to yourself");
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .isRead(false)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);
        return mapToDTO(savedMessage);
    }

    public Page<ChatMessageDTO> getConversation(User currentUser, UUID otherUserId, Pageable pageable) {
        return chatMessageRepository.findConversation(currentUser.getId(), otherUserId, pageable)
                .map(this::mapToDTO);
    }

    public List<ChatMessageDTO> getRecentConversations(User currentUser) {
        return chatMessageRepository.findRecentConversations(currentUser.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(User currentUser, UUID senderId) {
        chatMessageRepository.markAsRead(senderId, currentUser.getId());
    }

    private ChatMessageDTO mapToDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getFullName())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isRead(message.isRead())
                .build();
    }
}
