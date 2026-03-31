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
public class ReferralStatusHistoryDTO {
    private UUID id;
    private UUID referralId;
    private Referral.ReferralStatus oldStatus;
    private Referral.ReferralStatus newStatus;
    private String note;
    private UUID changedBy;
    private String changerName;
    private LocalDateTime changedAt;
}
