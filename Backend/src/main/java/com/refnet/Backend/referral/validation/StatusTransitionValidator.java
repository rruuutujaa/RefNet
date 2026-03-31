package com.refnet.Backend.referral.validation;

import com.refnet.Backend.common.exception.AppException;
import com.refnet.Backend.referral.entity.Referral;

import java.util.List;
import java.util.Map;

public class StatusTransitionValidator {

    private static final Map<Referral.ReferralStatus, List<Referral.ReferralStatus>> ALLOWED_TRANSITIONS = Map.of(
        Referral.ReferralStatus.SUBMITTED, List.of(Referral.ReferralStatus.UNDER_REVIEW),
        Referral.ReferralStatus.UNDER_REVIEW, List.of(Referral.ReferralStatus.ACCEPTED, Referral.ReferralStatus.NOT_SELECTED),
        Referral.ReferralStatus.ACCEPTED, List.of(Referral.ReferralStatus.HIRED, Referral.ReferralStatus.NOT_SELECTED)
    );

    public static void validate(Referral.ReferralStatus currentStatus, Referral.ReferralStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }

        List<Referral.ReferralStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        
        if (allowed == null || !allowed.contains(nextStatus)) {
            String allowedStr = allowed == null ? "None" : allowed.toString();
            throw new AppException(String.format("Invalid status transition from %s to %s. Allowed transitions: %s", 
                currentStatus, nextStatus, allowedStr));
        }
    }
}
