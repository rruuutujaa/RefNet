package com.refnet.Backend.referral.controller;

import com.refnet.Backend.referral.dto.ReferralRequestCreateRequest;
import com.refnet.Backend.referral.dto.ReferralRequestDTO;
import com.refnet.Backend.referral.service.ReferralRequestService;
import com.refnet.Backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referral-requests")
@RequiredArgsConstructor
public class ReferralRequestController {

    private final ReferralRequestService referralRequestService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ReferralRequestDTO> createReferralRequest(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReferralRequestCreateRequest request
    ) {
        return ResponseEntity.ok(referralRequestService.createReferralRequest(currentUser, request));
    }

    @GetMapping("/sent")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ReferralRequestDTO>> getSentRequests(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(referralRequestService.getSentRequests(currentUser));
    }

    @GetMapping("/received")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<ReferralRequestDTO>> getReceivedRequests(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(referralRequestService.getReceivedRequests(currentUser));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> acceptRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        referralRequestService.acceptRequest(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/decline")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> declineRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String reason
    ) {
        referralRequestService.declineRequest(id, currentUser, reason);
        return ResponseEntity.ok().build();
    }
}
