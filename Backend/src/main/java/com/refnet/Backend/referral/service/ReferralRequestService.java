package com.refnet.Backend.referral.service;

import com.refnet.Backend.common.exception.AppException;
import com.refnet.Backend.common.exception.ResourceNotFoundException;
import com.refnet.Backend.job.entity.Job;
import com.refnet.Backend.job.repository.JobRepository;
import com.refnet.Backend.notification.entity.Notification;
import com.refnet.Backend.notification.service.NotificationService;
import com.refnet.Backend.referral.dto.ReferralRequestCreateRequest;
import com.refnet.Backend.referral.dto.ReferralRequestDTO;
import com.refnet.Backend.referral.entity.ReferralRequest;
import com.refnet.Backend.referral.repository.ReferralRequestRepository;
import com.refnet.Backend.user.entity.Role;
import com.refnet.Backend.user.entity.User;
import com.refnet.Backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralRequestService {

    private final ReferralRequestRepository referralRequestRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReferralRequestDTO createReferralRequest(User candidate, ReferralRequestCreateRequest request) {
        if (!candidate.getRoles().contains(Role.CANDIDATE)) {
            throw new AppException("Only candidates can create referral requests.");
        }

        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found."));

        if (candidate.getId().equals(employee.getId())) {
            throw new AppException("Candidate cannot send a referral request to themselves.");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found."));

        if (!employee.getCompanyId().equals(job.getCompanyId())) {
            throw new AppException("Employee must belong to the same company as the job.");
        }

        // Check for duplicate request
        referralRequestRepository.findByCandidateAndEmployeeAndJob(candidate, employee, job)
                .ifPresent(r -> {
                    throw new AppException("A referral request already exists for this job and employee.");
                });

        ReferralRequest referralRequest = ReferralRequest.builder()
                .candidate(candidate)
                .employee(employee)
                .job(job)
                .message(request.getMessage())
                .status(ReferralRequest.ReferralRequestStatus.PENDING)
                .build();

        ReferralRequest savedRequest = referralRequestRepository.save(referralRequest);

        // Notify employee about the new referral request
        String title = "New Referral Request Received";
        String body = String.format("Candidate %s has requested a referral for the position: %s", 
                candidate.getFullName(), job.getTitle());
        
        notificationService.createNotification(employee, Notification.NotificationType.REQUEST_RECEIVED, 
                title, body, savedRequest.getId());

        return mapToDTO(savedRequest);
    }

    public List<ReferralRequestDTO> getSentRequests(User candidate) {
        if (!candidate.getRoles().contains(Role.CANDIDATE)) {
            throw new AppException("Only candidates can view sent referral requests.");
        }
        return referralRequestRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReferralRequestDTO> getReceivedRequests(User employee) {
        if (!employee.getRoles().contains(Role.EMPLOYEE)) {
            throw new AppException("Only employees can view received referral requests.");
        }
        return referralRequestRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptRequest(UUID requestId, User employee) {
        ReferralRequest request = referralRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral request not found."));

        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new AppException("Unauthorized to accept this request.");
        }

        if (request.getStatus() != ReferralRequest.ReferralRequestStatus.PENDING) {
            throw new AppException("Only pending requests can be accepted.");
        }

        request.setStatus(ReferralRequest.ReferralRequestStatus.ACCEPTED);
        referralRequestRepository.save(request);

        // Notify candidate that their request was accepted
        String title = "Referral Request Accepted";
        String body = String.format("Employee %s has accepted your referral request for: %s. They will now submit a formal referral.", 
                employee.getFullName(), request.getJob().getTitle());
        
        notificationService.createNotification(request.getCandidate(), Notification.NotificationType.STATUS_CHANGED, 
                title, body, request.getId());
    }

    @Transactional
    public void declineRequest(UUID requestId, User employee, String reason) {
        ReferralRequest request = referralRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral request not found."));

        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new AppException("Unauthorized to decline this request.");
        }

        if (request.getStatus() != ReferralRequest.ReferralRequestStatus.PENDING) {
            throw new AppException("Only pending requests can be declined.");
        }

        request.setStatus(ReferralRequest.ReferralRequestStatus.DECLINED);
        request.setDeclineReason(reason);
        referralRequestRepository.save(request);

        // Notify candidate that their request was declined
        String title = "Referral Request Declined";
        String body = String.format("Employee %s has declined your referral request for: %s.", 
                employee.getFullName(), request.getJob().getTitle());
        if (reason != null && !reason.isBlank()) {
            body += " Reason: " + reason;
        }
        
        notificationService.createNotification(request.getCandidate(), Notification.NotificationType.STATUS_CHANGED, 
                title, body, request.getId());
    }

    private ReferralRequestDTO mapToDTO(ReferralRequest request) {
        return ReferralRequestDTO.builder()
                .id(request.getId())
                .candidateId(request.getCandidate().getId())
                .candidateName(request.getCandidate().getFullName())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFullName())
                .jobId(request.getJob().getId())
                .jobTitle(request.getJob().getTitle())
                .message(request.getMessage())
                .status(request.getStatus())
                .declineReason(request.getDeclineReason())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
