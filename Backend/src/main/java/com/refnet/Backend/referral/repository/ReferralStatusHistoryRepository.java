package com.refnet.Backend.referral.repository;

import com.refnet.Backend.referral.entity.ReferralStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReferralStatusHistoryRepository extends JpaRepository<ReferralStatusHistory, UUID> {
    
    List<ReferralStatusHistory> findByReferralIdOrderByChangedAtAsc(UUID referralId);
}
