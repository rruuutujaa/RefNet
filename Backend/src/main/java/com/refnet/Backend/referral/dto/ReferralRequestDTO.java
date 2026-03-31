package com.refnet.Backend.referral.dto;

import com.refnet.Backend.referral.entity.ReferralRequest;
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
public class ReferralRequestDTO {
    private UUID id;
    private UUID candidateId;
    private String candidateName;
    private UUID employeeId;
    private String employeeName;
    private UUID jobId;
    private String jobTitle;
    private String message;
    private ReferralRequest.ReferralRequestStatus status;
    private String declineReason;
    private LocalDateTime createdAt;
}
