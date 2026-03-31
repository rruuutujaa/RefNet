package com.refnet.Backend.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "candidate_profiles", indexes = {
    @Index(name = "idx_cp_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private User user;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String location;

    @ElementCollection
    @CollectionTable(
        name = "candidate_skills", 
        joinColumns = @JoinColumn(name = "profile_id"),
        indexes = @Index(name = "idx_cp_skill", columnList = "skill")
    )
    @Column(name = "skill")
    private List<String> skills;

    private Integer experienceYears;

    private String linkedinUrl;

    @Column(length = 1000)
    private String aboutMe;

    private String resumeUrl;

    private String profilePhotoUrl;

    @Builder.Default
    private boolean isOpenToReferral = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
