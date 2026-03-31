package com.refnet.Backend.referral.entity;

import com.refnet.Backend.job.entity.Job;
import com.refnet.Backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "referral_requests", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rr_candidate_employee_job", columnNames = {"candidate_id", "employee_id", "job_id"})
    },
    indexes = {
        @Index(name = "idx_rr_candidate_id", columnList = "candidate_id"),
        @Index(name = "idx_rr_employee_id", columnList = "employee_id"),
        @Index(name = "idx_rr_job_id", columnList = "job_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralRequest {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralRequestStatus status;

    private String declineReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum ReferralRequestStatus {
        PENDING, ACCEPTED, DECLINED
    }
}
