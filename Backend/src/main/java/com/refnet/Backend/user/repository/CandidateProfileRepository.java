package com.refnet.Backend.user.repository;

import com.refnet.Backend.user.entity.CandidateProfile;
import com.refnet.Backend.user.entity.User;
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
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {
    Optional<CandidateProfile> findByUser(User user);
    Optional<CandidateProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    @Query("SELECT cp FROM CandidateProfile cp JOIN cp.user u " +
           "WHERE (:query IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:skills IS NULL OR EXISTS (SELECT s FROM cp.skills s WHERE LOWER(s) IN :skills))")
    Page<CandidateProfile> searchProfiles(@Param("query") String query,
                                          @Param("skills") List<String> skills,
                                          Pageable pageable);
}
