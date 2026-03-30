package com.arundhati.clinic.controller;

import com.arundhati.clinic.dto.AdminAnalyticsDTO;
import com.arundhati.clinic.dto.PendingDoctorDTO;
import com.arundhati.clinic.entity.DoctorProfile;
import com.arundhati.clinic.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Get all pending (unverified) doctors
     * @return List of pending doctors with their details
     */
    @GetMapping("/doctors/pending")
    public ResponseEntity<List<PendingDoctorDTO>> getPendingDoctors() {
        log.debug("AdminController: GET /api/admin/doctors/pending");
        try {
            List<PendingDoctorDTO> pendingDoctors = adminService.getPendingDoctors();
            log.info("Retrieved {} pending doctors", pendingDoctors.size());
            return ResponseEntity.ok(pendingDoctors);
        } catch (Exception e) {
            log.error("Error retrieving pending doctors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * Verify a doctor profile by ID
     * @param profileId the doctor profile ID to verify
     * @return verified doctor profile
     */
    @PatchMapping("/doctors/{profileId}/verify")
    public ResponseEntity<?> verifyDoctor(@PathVariable Long profileId) {
        log.debug("AdminController: PATCH /api/admin/doctors/{}/verify", profileId);
        
        // Validate input
        if (profileId == null || profileId <= 0) {
            log.warn("Invalid profile ID: {}", profileId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    createErrorResponse("Invalid profile ID provided", HttpStatus.BAD_REQUEST.value())
            );
        }
        
        try {
            DoctorProfile verifiedDoctor = adminService.verifyDoctor(profileId);
            log.info("Successfully verified doctor profile: {}", profileId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor profile verified successfully");
            response.put("profileId", profileId);
            response.put("doctorName", verifiedDoctor.getUser().getName());
            response.put("verified", verifiedDoctor.isVerified());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument when verifying doctor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value())
            );
        } catch (Exception e) {
            log.error("Error verifying doctor profile", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    createErrorResponse("Doctor profile not found or already verified", HttpStatus.NOT_FOUND.value())
            );
        }
    }

    /**
     * Get system analytics
     * @return AdminAnalyticsDTO with system statistics
     */
    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsDTO> getAnalytics() {
        log.debug("AdminController: GET /api/admin/analytics");
        try {
            AdminAnalyticsDTO analytics = adminService.getAnalytics();
            log.info("Generated analytics - Pending doctors: {}", analytics.getPendingDoctorVerifications());
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error generating analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get count of pending doctors needing verification
     * @return count as a map
     */
    @GetMapping("/doctors/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingDoctorCount() {
        log.debug("AdminController: GET /api/admin/doctors/pending/count");
        try {
            long count = adminService.getPendingDoctorCount();
            Map<String, Long> response = new HashMap<>();
            response.put("pendingCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting pending doctor count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("pendingCount", 0L));
        }
    }

    /**
     * Create error response object
     */
    private Map<String, Object> createErrorResponse(String message, int status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", status);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}
