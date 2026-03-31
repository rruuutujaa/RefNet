package com.refnet.Backend.referral.service;

import com.refnet.Backend.common.exception.AppException;
import com.refnet.Backend.common.exception.ResourceNotFoundException;
import com.refnet.Backend.job.entity.Job;
import com.refnet.Backend.job.repository.JobRepository;
import com.refnet.Backend.notification.entity.Notification;
import com.refnet.Backend.notification.service.NotificationService;
import com.refnet.Backend.referral.dto.ReferralCreateRequest;
import com.refnet.Backend.referral.dto.ReferralDTO;
import com.refnet.Backend.referral.dto.ReferralStatusHistoryDTO;
import com.refnet.Backend.referral.dto.ReferralStatusUpdateRequest;
import com.refnet.Backend.referral.entity.Referral;
import com.refnet.Backend.referral.entity.ReferralRequest;
import com.refnet.Backend.referral.entity.ReferralStatusHistory;
import com.refnet.Backend.referral.repository.ReferralRepository;
import com.refnet.Backend.referral.repository.ReferralRequestRepository;
import com.refnet.Backend.referral.repository.ReferralStatusHistoryRepository;
import com.refnet.Backend.referral.validation.StatusTransitionValidator;
import com.refnet.Backend.user.entity.Role;
import com.refnet.Backend.user.entity.User;
import com.refnet.Backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final ReferralRequestRepository referralRequestRepository;
    private final ReferralStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReferralDTO createReferral(User employee, ReferralCreateRequest request) {
        if (!employee.getRoles().contains(Role.EMPLOYEE)) {
            throw new AppException("Only employees can submit referrals.");
        }

        User candidate = userRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found."));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found."));

        if (!employee.getCompanyId().equals(job.getCompanyId())) {
            throw new AppException("Employee must belong to the same company as the job.");
        }

        // Check for duplicate referral (candidate + job)
        referralRepository.findByCandidateAndJob(candidate, job)
                .ifPresent(r -> {
                    throw new AppException("A referral for this candidate and job already exists.");
                });

        ReferralRequest referralRequest = null;
        if (request.getReferralRequestId() != null) {
            referralRequest = referralRequestRepository.findById(request.getReferralRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Referral request not found."));

            if (referralRequest.getStatus() != ReferralRequest.ReferralRequestStatus.ACCEPTED) {
                throw new AppException("Referral request must be ACCEPTED before creating a referral.");
            }
        }

        Referral referral = Referral.builder()
                .referralRequest(referralRequest)
                .referrer(employee)
                .candidate(candidate)
                .job(job)
                .recommendationNote(request.getRecommendationNote())
                .relationship(request.getRelationship())
                .status(Referral.ReferralStatus.SUBMITTED)
                .build();

        Referral savedReferral = referralRepository.save(referral);

        // Save initial history
        saveStatusHistory(savedReferral, null, Referral.ReferralStatus.SUBMITTED, "Initial referral submission", employee.getId());

        // Notify candidate and employee
        String candidateTitle = "New Referral Submitted for You";
        String candidateBody = String.format("Employee %s has submitted a formal referral for you for the position: %s", 
                employee.getFullName(), job.getTitle());
        notificationService.createNotification(candidate, Notification.NotificationType.REFERRAL_SUBMITTED, 
                candidateTitle, candidateBody, savedReferral.getId());

        String employeeTitle = "Referral Successfully Submitted";
        String employeeBody = String.format("You have successfully submitted a referral for %s for the position: %s", 
                candidate.getFullName(), job.getTitle());
        notificationService.createNotification(employee, Notification.NotificationType.REFERRAL_SUBMITTED, 
                employeeTitle, employeeBody, savedReferral.getId());

        return mapToDTO(savedReferral);
    }

    public List<ReferralDTO> getMyReferrals(User employee) {
        if (!employee.getRoles().contains(Role.EMPLOYEE)) {
            throw new AppException("Only employees can view their submitted referrals.");
        }
        return referralRepository.findByReferrerId(employee.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReferralDTO> getReceivedReferrals(User candidate) {
        if (!candidate.getRoles().contains(Role.CANDIDATE)) {
            throw new AppException("Only candidates can view their received referrals.");
        }
        return referralRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ReferralDTO> getCompanyReferrals(User hrAdmin, Pageable pageable, Referral.ReferralStatus status) {
        if (!hrAdmin.getRoles().contains(Role.HR_ADMIN) && !hrAdmin.getRoles().contains(Role.ADMIN)) {
            throw new AppException("Unauthorized to view company referrals.");
        }
        
        UUID companyId = hrAdmin.getRoles().contains(Role.ADMIN) ? null : hrAdmin.getCompanyId();
        return referralRepository.searchReferrals(companyId, status, pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public void updateReferralStatus(UUID referralId, User hrAdmin, ReferralStatusUpdateRequest request) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found."));

        if (!hrAdmin.getRoles().contains(Role.ADMIN)) {
            if (!hrAdmin.getRoles().contains(Role.HR_ADMIN) || !hrAdmin.getCompanyId().equals(referral.getJob().getCompanyId())) {
                throw new AppException("Unauthorized to update status for this referral.");
            }
        }

        Referral.ReferralStatus oldStatus = referral.getStatus();
        Referral.ReferralStatus newStatus = request.getNewStatus();

        StatusTransitionValidator.validate(oldStatus, newStatus);

        referral.setStatus(newStatus);
        referralRepository.save(referral);

        saveStatusHistory(referral, oldStatus, newStatus, request.getNote(), hrAdmin.getId());

        // Notify candidate and referrer about the status change
        String title = "Referral Status Updated";
        String body = String.format("The status of the referral for %s has been updated from %s to %s.", 
                referral.getJob().getTitle(), oldStatus, newStatus);
        if (request.getNote() != null && !request.getNote().isBlank()) {
            body += " Note: " + request.getNote();
        }

        notificationService.createNotification(referral.getCandidate(), Notification.NotificationType.STATUS_CHANGED, 
                title, body, referral.getId());
        notificationService.createNotification(referral.getReferrer(), Notification.NotificationType.STATUS_CHANGED, 
                title, body, referral.getId());
    }

    public List<ReferralStatusHistoryDTO> getReferralHistory(UUID referralId, User currentUser) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found."));

        // RBAC: Only candidate, referrer, HR of that company, or Super Admin can view history
        boolean isCandidate = referral.getCandidate().getId().equals(currentUser.getId());
        boolean isReferrer = referral.getReferrer().getId().equals(currentUser.getId());
        boolean isHrOfCompany = currentUser.getRoles().contains(Role.HR_ADMIN) && 
                                referral.getJob().getCompanyId().equals(currentUser.getCompanyId());
        boolean isAdmin = currentUser.getRoles().contains(Role.ADMIN);

        if (!isCandidate && !isReferrer && !isHrOfCompany && !isAdmin) {
            throw new AppException("Unauthorized to view history for this referral.");
        }

        return statusHistoryRepository.findByReferralIdOrderByChangedAtAsc(referralId)
                .stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());
    }

    private void saveStatusHistory(Referral referral, Referral.ReferralStatus oldStatus, Referral.ReferralStatus newStatus, String note, UUID changedBy) {
        ReferralStatusHistory history = ReferralStatusHistory.builder()
                .referral(referral)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .note(note)
                .changedBy(changedBy)
                .build();
        statusHistoryRepository.save(history);
    }

    private ReferralDTO mapToDTO(Referral referral) {
        return ReferralDTO.builder()
                .id(referral.getId())
                .referralRequestId(referral.getReferralRequest() != null ? referral.getReferralRequest().getId() : null)
                .referrerId(referral.getReferrer().getId())
                .referrerName(referral.getReferrer().getFullName())
                .candidateId(referral.getCandidate().getId())
                .candidateName(referral.getCandidate().getFullName())
                .jobId(referral.getJob().getId())
                .jobTitle(referral.getJob().getTitle())
                .recommendationNote(referral.getRecommendationNote())
                .relationship(referral.getRelationship())
                .status(referral.getStatus())
                .submittedAt(referral.getSubmittedAt())
                .updatedAt(referral.getUpdatedAt())
                .build();
    }

    private ReferralStatusHistoryDTO mapHistoryToDTO(ReferralStatusHistory history) {
        User changer = userRepository.findById(history.getChangedBy()).orElse(null);
        return ReferralStatusHistoryDTO.builder()
                .id(history.getId())
                .referralId(history.getReferral().getId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .note(history.getNote())
                .changedBy(history.getChangedBy())
                .changerName(changer != null ? changer.getFullName() : "Unknown")
                .changedAt(history.getChangedAt())
                .build();
    }
}
