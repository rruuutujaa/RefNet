package com.refnet.Backend.chat.controller;

import com.refnet.Backend.chat.dto.ChatMessageDTO;
import com.refnet.Backend.chat.service.ChatService;
import com.refnet.Backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChatMessageDTO>> getConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(chatService.getConversation(currentUser, userId, pageable));
    }

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageDTO>> getRecentConversations(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(chatService.getRecentConversations(currentUser));
    }

    @PatchMapping("/read/{senderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID senderId
    ) {
        chatService.markAsRead(currentUser, senderId);
        return ResponseEntity.ok().build();
    }
}
