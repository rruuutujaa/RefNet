package com.refnet.Backend.user.dto;

import com.refnet.Backend.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private Set<Role> roles;
    private UUID companyId;
    private boolean isEmailVerified;
    private LocalDateTime createdAt;
    private CandidateProfileResponse profile;
}
