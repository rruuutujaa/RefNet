package com.refnet.Backend.user.service;

import com.refnet.Backend.common.exception.AppException;
import com.refnet.Backend.common.exception.ResourceNotFoundException;
import com.refnet.Backend.user.dto.CandidateProfileResponse;
import com.refnet.Backend.user.dto.CandidateProfileUpdateRequest;
import com.refnet.Backend.user.dto.UserResponse;
import com.refnet.Backend.user.entity.CandidateProfile;
import com.refnet.Backend.user.entity.Role;
import com.refnet.Backend.user.entity.User;
import com.refnet.Backend.user.repository.CandidateProfileRepository;
import com.refnet.Backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    public UserResponse getUserMe(User currentUser) {
        CandidateProfileResponse profile = null;
        if (currentUser.getRoles().contains(Role.CANDIDATE)) {
            profile = candidateProfileRepository.findByUser(currentUser)
                    .map(this::mapToProfileResponse)
                    .orElse(null);
        }
        return mapToUserResponse(currentUser, profile);
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CandidateProfileResponse profile = null;
        if (user.getRoles().contains(Role.CANDIDATE)) {
            profile = candidateProfileRepository.findByUser(user)
                    .map(this::mapToProfileResponse)
                    .orElse(null);
        }
        
        return mapToUserResponse(user, profile);
    }

    @Transactional
    public CandidateProfileResponse updateCandidateProfile(User currentUser, CandidateProfileUpdateRequest request) {
        if (!currentUser.getRoles().contains(Role.CANDIDATE)) {
            throw new AppException("Only candidates can have profiles.");
        }

        CandidateProfile profile = candidateProfileRepository.findByUser(currentUser)
                .orElse(CandidateProfile.builder().user(currentUser).build());

        profile.setPhone(request.getPhone());
        profile.setLocation(request.getLocation());
        profile.setSkills(request.getSkills());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setAboutMe(request.getAboutMe());
        profile.setOpenToReferral(request.isOpenToReferral());

        CandidateProfile savedProfile = candidateProfileRepository.save(profile);
        return mapToProfileResponse(savedProfile);
    }

    @Transactional
    public void updateResumeUrl(User currentUser, String resumeUrl) {
        CandidateProfile profile = candidateProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Create profile first."));
        profile.setResumeUrl(resumeUrl);
        candidateProfileRepository.save(profile);
    }

    @Transactional
    public void updatePhotoUrl(User currentUser, String photoUrl) {
        CandidateProfile profile = candidateProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Create profile first."));
        profile.setProfilePhotoUrl(photoUrl);
        candidateProfileRepository.save(profile);
    }

    public Page<CandidateProfileResponse> searchProfiles(String query, List<String> skills, Pageable pageable) {
        return candidateProfileRepository.searchProfiles(query, skills, pageable)
                .map(this::mapToProfileResponse);
    }

    private UserResponse mapToUserResponse(User user, CandidateProfileResponse profile) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles())
                .companyId(user.getCompanyId())
                .isEmailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .profile(profile)
                .build();
    }

    private CandidateProfileResponse mapToProfileResponse(CandidateProfile profile) {
        return CandidateProfileResponse.builder()
                .id(profile.getId())
                .phone(profile.getPhone())
                .location(profile.getLocation())
                .skills(profile.getSkills())
                .experienceYears(profile.getExperienceYears())
                .linkedinUrl(profile.getLinkedinUrl())
                .aboutMe(profile.getAboutMe())
                .resumeUrl(profile.getResumeUrl())
                .profilePhotoUrl(profile.getProfilePhotoUrl())
                .isOpenToReferral(profile.isOpenToReferral())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
