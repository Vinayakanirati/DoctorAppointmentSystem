package com.arundhati.clinic.repository;

import com.arundhati.clinic.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    Optional<DoctorProfile> findByUserId(Long userId);
    
    // Get all unverified doctors efficiently using database query
    java.util.List<DoctorProfile> findByVerifiedFalse();
    
    // Count unverified doctors
    long countByVerifiedFalse();
}
