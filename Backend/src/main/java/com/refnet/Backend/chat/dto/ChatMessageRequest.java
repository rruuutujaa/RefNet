package com.refnet.Backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {
    @NotNull(message = "Receiver ID is required")
    private UUID receiverId;

    @NotBlank(message = "Content cannot be empty")
    private String content;
}
