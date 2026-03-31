package com.refnet.Backend.referral.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "referral_status_history", indexes = {
    @Index(name = "idx_rsh_referral_id", columnList = "referral_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralStatusHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_id", nullable = false)
    private Referral referral;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Referral.ReferralStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Referral.ReferralStatus newStatus;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private UUID changedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime changedAt;
}
