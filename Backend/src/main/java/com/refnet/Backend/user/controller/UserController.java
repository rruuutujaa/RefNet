package com.refnet.Backend.user.controller;

import com.refnet.Backend.common.util.PaginationUtil;
import com.refnet.Backend.common.service.FileStorageService;
import com.refnet.Backend.user.dto.CandidateProfileResponse;
import com.refnet.Backend.user.dto.CandidateProfileUpdateRequest;
import com.refnet.Backend.user.dto.UserResponse;
import com.refnet.Backend.user.entity.User;
import com.refnet.Backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getUserMe(currentUser));
    }

    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateProfileResponse> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CandidateProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateCandidateProfile(currentUser, request));
    }

    @PostMapping("/me/resume")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> uploadResume(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        String resumeUrl = fileStorageService.storeResume(file);
        userService.updateResumeUrl(currentUser, resumeUrl);
        return ResponseEntity.ok(resumeUrl);
    }

    @PostMapping("/me/photo")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> uploadPhoto(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        String photoUrl = fileStorageService.storePhoto(file);
        userService.updatePhotoUrl(currentUser, photoUrl);
        return ResponseEntity.ok(photoUrl);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<CandidateProfileResponse>> searchProfiles(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> skills,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchProfiles(q, skills, PaginationUtil.limitPageSize(pageable)));
    }
}
