package com.refnet.Backend.chat.controller;

import com.refnet.Backend.chat.dto.ChatMessageDTO;
import com.refnet.Backend.chat.dto.ChatMessageRequest;
import com.refnet.Backend.chat.service.ChatService;
import com.refnet.Backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            log.error("Unauthorized attempt to send message");
            return;
        }

        // Get authenticated user from principal (set by WebSocketConfig interceptor)
        User sender = (User) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) principal).getPrincipal();
        
        try {
            ChatMessageDTO savedMessage = chatService.saveMessage(sender, request);

            // Send to receiver: /user/{receiverId}/queue/messages
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getReceiverId().toString(),
                    "/queue/messages",
                    savedMessage
            );

            // Send back to sender (ack): /user/{senderId}/queue/messages
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getSenderId().toString(),
                    "/queue/messages",
                    savedMessage
            );
            
            log.info("Message sent from {} to {}", sender.getEmail(), request.getReceiverId());
        } catch (Exception e) {
            log.error("Failed to process chat message: {}", e.getMessage());
            // Send error back to sender
            messagingTemplate.convertAndSendToUser(
                    sender.getId().toString(),
                    "/queue/errors",
                    e.getMessage()
            );
        }
    }
}
