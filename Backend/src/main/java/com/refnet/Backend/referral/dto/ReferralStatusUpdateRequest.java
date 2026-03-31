package com.refnet.Backend.referral.dto;

import com.refnet.Backend.referral.entity.Referral;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferralStatusUpdateRequest {

    @NotNull(message = "New status is required")
    private Referral.ReferralStatus newStatus;

    private String note;
}
