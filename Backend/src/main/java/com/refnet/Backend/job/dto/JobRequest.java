package com.refnet.Backend.job.dto;

import com.refnet.Backend.job.entity.Job;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters long")
    private String description;

    @NotEmpty(message = "At least one skill is required")
    private List<String> skillsRequired;

    @Min(value = 0, message = "Minimum experience cannot be negative")
    @Max(value = 30, message = "Minimum experience cannot exceed 30")
    private Integer experienceMin;

    @NotNull(message = "Job status is required")
    private Job.JobStatus status;
}
