package com.refnet.Backend.referral.entity;

import com.refnet.Backend.job.entity.Job;
import com.refnet.Backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "referrals", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_referral_referrer_candidate_job", columnNames = {"referrer_id", "candidate_id", "job_id"})
    },
    indexes = {
        @Index(name = "idx_ref_referrer_id", columnList = "referrer_id"),
        @Index(name = "idx_ref_candidate_id", columnList = "candidate_id"),
        @Index(name = "idx_ref_job_id", columnList = "job_id"),
        @Index(name = "idx_ref_company_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_request_id")
    private ReferralRequest referralRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", nullable = false)
    private User referrer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendationNote;

    private String relationship;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ReferralStatus {
        SUBMITTED, UNDER_REVIEW, ACCEPTED, HIRED, NOT_SELECTED
    }
}
