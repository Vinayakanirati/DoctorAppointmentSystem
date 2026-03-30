package com.arundhati.clinic.controller;

import com.arundhati.clinic.dto.AppointmentDTO;
import com.arundhati.clinic.dto.DashboardStats;
import com.arundhati.clinic.dto.SlotDTO;
import com.arundhati.clinic.dto.SlotRequest;
import com.arundhati.clinic.entity.AppointmentStatus;
import com.arundhati.clinic.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/slots")
    public ResponseEntity<SlotDTO> createSlot(
            Authentication authentication,
            @Valid @RequestBody SlotRequest request
    ) {
        return ResponseEntity.ok(doctorService.createSlot(authentication.getName(), request));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<SlotDTO>> getMySlots(Authentication authentication) {
        return ResponseEntity.ok(doctorService.getMySlots(authentication.getName()));
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<String> deleteSlot(
            Authentication authentication,
            @PathVariable Long slotId
    ) {
        doctorService.deleteSlot(authentication.getName(), slotId);
        return ResponseEntity.ok("Slot deleted successfully");
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDTO>> getDailyAppointments(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(doctorService.getMyDailyAppointments(authentication.getName(), date));
    }

    @PatchMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            Authentication authentication,
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus status
    ) {
        return ResponseEntity.ok(doctorService.updateAppointmentStatus(authentication.getName(), appointmentId, status));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats(Authentication authentication) {
        return ResponseEntity.ok(doctorService.getDashboardStats(authentication.getName()));
    }
}
