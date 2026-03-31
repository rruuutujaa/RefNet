package com.refnet.Backend.job.dto;

import com.refnet.Backend.job.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobResponse {
    private UUID id;
    private UUID companyId;
    private String title;
    private String department;
    private String description;
    private List<String> skillsRequired;
    private Integer experienceMin;
    private Job.JobStatus status;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
