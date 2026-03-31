package com.refnet.Backend.referral.controller;

import com.refnet.Backend.common.util.PaginationUtil;
import com.refnet.Backend.referral.dto.ReferralCreateRequest;
import com.refnet.Backend.referral.dto.ReferralDTO;
import com.refnet.Backend.referral.dto.ReferralStatusHistoryDTO;
import com.refnet.Backend.referral.dto.ReferralStatusUpdateRequest;
import com.refnet.Backend.referral.entity.Referral;
import com.refnet.Backend.referral.service.ReferralService;
import com.refnet.Backend.user.entity.User;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ReferralDTO> submitReferral(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReferralCreateRequest request
    ) {
        return ResponseEntity.ok(referralService.createReferral(currentUser, request));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<ReferralDTO>> getMyReferrals(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(referralService.getMyReferrals(currentUser));
    }

    @GetMapping("/received")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ReferralDTO>> getReceivedReferrals(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(referralService.getReceivedReferrals(currentUser));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<ReferralDTO>> getCompanyReferrals(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Referral.ReferralStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(referralService.getCompanyReferrals(currentUser, PaginationUtil.limitPageSize(pageable), status));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> updateReferralStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReferralStatusUpdateRequest request
    ) {
        referralService.updateReferralStatus(id, currentUser, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReferralStatusHistoryDTO>> getReferralHistory(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(referralService.getReferralHistory(id, currentUser));
    }
}
