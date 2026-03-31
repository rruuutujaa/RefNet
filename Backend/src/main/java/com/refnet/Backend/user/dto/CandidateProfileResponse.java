package com.refnet.Backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CandidateProfileResponse {
    private UUID id;
    private String phone;
    private String location;
    private List<String> skills;
    private Integer experienceYears;
    private String linkedinUrl;
    private String aboutMe;
    private String resumeUrl;
    private String profilePhotoUrl;
    private boolean isOpenToReferral;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
