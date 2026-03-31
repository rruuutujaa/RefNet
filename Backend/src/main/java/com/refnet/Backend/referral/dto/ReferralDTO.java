package com.refnet.Backend.referral.dto;

import com.refnet.Backend.referral.entity.Referral;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferralDTO {
    private UUID id;
    private UUID referralRequestId;
    private UUID referrerId;
    private String referrerName;
    private UUID candidateId;
    private String candidateName;
    private UUID jobId;
    private String jobTitle;
    private String recommendationNote;
    private String relationship;
    private Referral.ReferralStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}
