package com.refnet.Backend.job.repository;

import com.refnet.Backend.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    
    @Query("SELECT j FROM Job j " +
           "WHERE (:companyId IS NULL OR j.companyId = :companyId) " +
           "AND (:status IS NULL OR j.status = :status) " +
           "AND (:skills IS NULL OR EXISTS (SELECT s FROM j.skillsRequired s WHERE LOWER(s) IN :skills)) " +
           "AND (:experienceMin IS NULL OR j.experienceMin <= :experienceMin) " +
           "ORDER BY j.createdAt DESC")
    Page<Job> searchJobs(@Param("companyId") UUID companyId,
                         @Param("status") Job.JobStatus status,
                         @Param("skills") List<String> skills,
                         @Param("experienceMin") Integer experienceMin,
                         Pageable pageable);
}
