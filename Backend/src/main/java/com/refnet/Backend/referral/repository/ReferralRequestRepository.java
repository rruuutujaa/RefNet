package com.refnet.Backend.referral.repository;

import com.refnet.Backend.referral.entity.ReferralRequest;
import com.refnet.Backend.user.entity.User;
import com.refnet.Backend.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferralRequestRepository extends JpaRepository<ReferralRequest, UUID> {
    
    List<ReferralRequest> findByCandidateId(UUID candidateId);
    
    List<ReferralRequest> findByEmployeeId(UUID employeeId);
    
    Optional<ReferralRequest> findByCandidateAndEmployeeAndJobAndStatus(
            User candidate, User employee, Job job, ReferralRequest.ReferralRequestStatus status);
            
    Optional<ReferralRequest> findByCandidateAndEmployeeAndJob(User candidate, User employee, Job job);
}
