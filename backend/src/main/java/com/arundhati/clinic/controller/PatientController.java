package com.arundhati.clinic.controller;

import com.arundhati.clinic.dto.AppointmentDTO;
import com.arundhati.clinic.dto.BookAppointmentRequest;
import com.arundhati.clinic.dto.DoctorDTO;
import com.arundhati.clinic.dto.SlotDTO;
import com.arundhati.clinic.entity.ConsultationMode;
import com.arundhati.clinic.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDTO>> browseDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) ConsultationMode mode
    ) {
        return ResponseEntity.ok(patientService.browseDoctors(specialty, mode));
    }

    @GetMapping("/doctors/{doctorId}/slots")
    public ResponseEntity<List<SlotDTO>> getDoctorSlots(@PathVariable Long doctorId) {
        return ResponseEntity.ok(patientService.getDoctorAvailableSlots(doctorId));
    }

    @PostMapping("/appointments/book")
    public ResponseEntity<AppointmentDTO> bookAppointment(
            Authentication authentication,
            @Valid @RequestBody BookAppointmentRequest request
    ) {
        return ResponseEntity.ok(patientService.bookAppointment(authentication.getName(), request));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDTO>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(patientService.getMyHistory(authentication.getName()));
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<String> cancelAppointment(
            Authentication authentication,
            @PathVariable Long appointmentId
    ) {
        patientService.cancelAppointment(authentication.getName(), appointmentId);
        return ResponseEntity.ok("Appointment cancelled successfully");
    }
}
