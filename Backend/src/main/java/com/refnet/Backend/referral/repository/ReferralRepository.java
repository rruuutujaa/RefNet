package com.refnet.Backend.referral.repository;

import com.refnet.Backend.referral.entity.Referral;
import com.refnet.Backend.user.entity.User;
import com.refnet.Backend.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, UUID> {
    
    List<Referral> findByReferrerId(UUID referrerId);
    
    List<Referral> findByCandidateId(UUID candidateId);
    
    Optional<Referral> findByCandidateAndJob(User candidate, Job job);
    
    @Query("SELECT r FROM Referral r JOIN r.job j " +
           "WHERE (:companyId IS NULL OR j.companyId = :companyId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "ORDER BY r.submittedAt DESC")
    Page<Referral> searchReferrals(@Param("companyId") UUID companyId, 
                                   @Param("status") Referral.ReferralStatus status, 
                                   Pageable pageable);
}
