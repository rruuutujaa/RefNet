package com.refnet.Backend.job.controller;

import com.refnet.Backend.common.util.PaginationUtil;
import com.refnet.Backend.job.dto.JobRequest;
import com.refnet.Backend.job.dto.JobResponse;
import com.refnet.Backend.job.entity.Job;
import com.refnet.Backend.job.service.JobService;
import com.refnet.Backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) Job.JobStatus status,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) Integer experienceMin,
            Pageable pageable
    ) {
        return ResponseEntity.ok(jobService.searchJobs(companyId, status, skills, experienceMin, PaginationUtil.limitPageSize(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN')")
    public ResponseEntity<JobResponse> createJob(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody JobRequest request
    ) {
        return ResponseEntity.ok(jobService.createJob(currentUser, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody JobRequest request
    ) {
        return ResponseEntity.ok(jobService.updateJob(id, currentUser, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> updateJobStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser,
            @RequestParam Job.JobStatus status
    ) {
        jobService.updateJobStatus(id, currentUser, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJob(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        jobService.deleteJob(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
