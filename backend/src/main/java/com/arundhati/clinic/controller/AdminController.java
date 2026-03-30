package com.arundhati.clinic.controller;

import com.arundhati.clinic.dto.AdminAnalyticsDTO;
import com.arundhati.clinic.entity.DoctorProfile;
import com.arundhati.clinic.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/doctors/pending")
    public ResponseEntity<List<DoctorProfile>> getPendingDoctors() {
        return ResponseEntity.ok(adminService.getPendingDoctors());
    }

    @PatchMapping("/doctors/{profileId}/verify")
    public ResponseEntity<DoctorProfile> verifyDoctor(@PathVariable Long profileId) {
        return ResponseEntity.ok(adminService.verifyDoctor(profileId));
    }

    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsDTO> getAnalytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }
}
