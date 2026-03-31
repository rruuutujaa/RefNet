package com.refnet.Backend.job.service;

import com.refnet.Backend.common.exception.AppException;
import com.refnet.Backend.common.exception.ResourceNotFoundException;
import com.refnet.Backend.job.dto.JobRequest;
import com.refnet.Backend.job.dto.JobResponse;
import com.refnet.Backend.job.entity.Job;
import com.refnet.Backend.job.repository.JobRepository;
import com.refnet.Backend.user.entity.Role;
import com.refnet.Backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", allEntries = true),
        @CacheEvict(value = "job_details", allEntries = true)
    })
    public JobResponse createJob(User currentUser, JobRequest request) {
        validateJobManagementAccess(currentUser, request.getCompanyId());

        Job job = Job.builder()
                .companyId(request.getCompanyId())
                .title(request.getTitle())
                .department(request.getDepartment())
                .description(request.getDescription())
                .skillsRequired(request.getSkillsRequired())
                .experienceMin(request.getExperienceMin())
                .status(request.getStatus())
                .createdBy(currentUser.getId())
                .build();

        Job savedJob = jobRepository.save(job);
        return mapToJobResponse(savedJob);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", allEntries = true),
        @CacheEvict(value = "job_details", key = "#jobId")
    })
    public JobResponse updateJob(UUID jobId, User currentUser, JobRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        validateJobManagementAccess(currentUser, job.getCompanyId());

        job.setTitle(request.getTitle());
        job.setDepartment(request.getDepartment());
        job.setDescription(request.getDescription());
        job.setSkillsRequired(request.getSkillsRequired());
        job.setExperienceMin(request.getExperienceMin());
        job.setStatus(request.getStatus());

        Job savedJob = jobRepository.save(job);
        return mapToJobResponse(savedJob);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", allEntries = true),
        @CacheEvict(value = "job_details", key = "#jobId")
    })
    public void updateJobStatus(UUID jobId, User currentUser, Job.JobStatus status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        validateJobManagementAccess(currentUser, job.getCompanyId());

        job.setStatus(status);
        jobRepository.save(job);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", allEntries = true),
        @CacheEvict(value = "job_details", key = "#jobId")
    })
    public void deleteJob(UUID jobId, User currentUser) {
        if (!currentUser.getRoles().contains(Role.ADMIN)) {
            throw new AppException("Only admins can delete jobs.");
        }
        jobRepository.deleteById(jobId);
    }

    @Cacheable(value = "job_details", key = "#jobId")
    public JobResponse getJobById(UUID jobId) {
        return jobRepository.findById(jobId)
                .map(this::mapToJobResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @Cacheable(value = "jobs", key = "T(java.util.Objects).hash(#companyId, #status, #skills, #experienceMin, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    public Page<JobResponse> searchJobs(UUID companyId, Job.JobStatus status, List<String> skills, Integer experienceMin, Pageable pageable) {
        return jobRepository.searchJobs(companyId, status, skills, experienceMin, pageable)
                .map(this::mapToJobResponse);
    }

    private void validateJobManagementAccess(User user, UUID companyId) {
        if (user.getRoles().contains(Role.ADMIN)) return;
        
        if (user.getRoles().contains(Role.HR_ADMIN)) {
            if (user.getCompanyId() != null && user.getCompanyId().equals(companyId)) {
                return;
            }
            throw new AppException("HR Admins can only manage jobs for their own company.");
        }
        
        throw new AppException("Unauthorized to manage jobs.");
    }

    private JobResponse mapToJobResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .title(job.getTitle())
                .department(job.getDepartment())
                .description(job.getDescription())
                .skillsRequired(job.getSkillsRequired())
                .experienceMin(job.getExperienceMin())
                .status(job.getStatus())
                .createdBy(job.getCreatedBy())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
