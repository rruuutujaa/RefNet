package com.refnet.Backend.referral.dto;

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
public class ReferralCreateRequest {

    private UUID referralRequestId; // Nullable if direct referral

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    @NotBlank(message = "Recommendation note is required")
    private String recommendationNote;

    private String relationship;
}
