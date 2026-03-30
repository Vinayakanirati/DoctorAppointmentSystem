package com.arundhati.clinic.service;

import com.arundhati.clinic.dto.AdminAnalyticsDTO;
import com.arundhati.clinic.dto.PendingDoctorDTO;
import com.arundhati.clinic.entity.Appointment;
import com.arundhati.clinic.entity.DoctorProfile;
import com.arundhati.clinic.entity.User;
import com.arundhati.clinic.entity.Role;
import com.arundhati.clinic.exception.BusinessException;
import com.arundhati.clinic.repository.AppointmentRepository;
import com.arundhati.clinic.repository.AuditLogRepository;
import com.arundhati.clinic.repository.DoctorProfileRepository;
import com.arundhati.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;

    /**
     * Get all pending (unverified) doctors with proper validation
     * Uses database query instead of loading all and filtering in memory
     * @return List of pending doctors as DTOs
     */
    public List<PendingDoctorDTO> getPendingDoctors() {
        log.info("Fetching pending doctor profiles");
        
        List<DoctorProfile> pendingDoctors = doctorProfileRepository.findByVerifiedFalse();
        
        if (pendingDoctors == null || pendingDoctors.isEmpty()) {
            log.debug("No pending doctors found");
            return List.of();
        }
        
        return pendingDoctors.stream()
                .filter(Objects::nonNull)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert DoctorProfile entity to PendingDoctorDTO
     */
    private PendingDoctorDTO convertToDTO(DoctorProfile profile) {
        if (profile == null || profile.getUser() == null) {
            log.warn("Invalid doctor profile found");
            return null;
        }
        
        String timeAgo = formatTimeAgo(profile.getUser().getCreatedAt());
        
        return PendingDoctorDTO.builder()
                .id(profile.getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .specialty(profile.getSpecialty())
                .mode(profile.getMode())
                .fees(profile.getFees())
                .phone(profile.getUser().getPhone())
                .verified(profile.isVerified())
                .registrationTimeAgo(timeAgo)
                .build();
    }

    /**
     * Format timestamp as human-readable time ago
     */
    private String formatTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "Unknown";
        }
        
        long daysAgo = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        long hoursAgo = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
        long minutesAgo = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
        
        if (daysAgo > 0) {
            return daysAgo + " day" + (daysAgo > 1 ? "s" : "") + " ago";
        } else if (hoursAgo > 0) {
            return hoursAgo + " hour" + (hoursAgo > 1 ? "s" : "") + " ago";
        } else if (minutesAgo > 0) {
            return minutesAgo + " minute" + (minutesAgo > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    /**
     * Verify a doctor profile by ID with validation
     * @param profileId the doctor profile ID
     * @return verified DoctorProfile
     */
    @Transactional
    public DoctorProfile verifyDoctor(Long profileId) {
        // Validate input
        if (profileId == null || profileId <= 0) {
            log.warn("Invalid profile ID provided: {}", profileId);
            throw new BusinessException("Invalid profile ID", HttpStatus.BAD_REQUEST);
        }
        
        log.info("Verifying doctor profile with ID: {}", profileId);
        
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> {
                    log.warn("Doctor profile not found with ID: {}", profileId);
                    return new BusinessException("Doctor profile not found", HttpStatus.NOT_FOUND);
                });

        // Check if already verified
        if (profile.isVerified()) {
            log.warn("Doctor profile {} is already verified", profileId);
            throw new BusinessException("Doctor profile is already verified", HttpStatus.CONFLICT);
        }

        profile.setVerified(true);
        DoctorProfile verifiedProfile = doctorProfileRepository.save(profile);
        
        log.info("Doctor profile {} verified successfully", profileId);

        // Send email notification
        try {
            if (profile.getUser() != null && profile.getUser().getEmail() != null) {
                emailService.sendEmail(
                        profile.getUser().getEmail(),
                        "Profile Verified - Arundhati Clinic",
                        "Congratulations! Your doctor profile has been verified by the Admin. You can now login and create slots."
                );
                log.info("Verification email sent to {}", profile.getUser().getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", profile.getUser().getEmail(), e.getMessage());
            // Don't throw exception, verification was successful even if email fails
        }

        return verifiedProfile;
    }

    /**
     * Get all verified doctors
     */
    public List<PendingDoctorDTO> getAllDoctors() {
        return doctorProfileRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all registered patients
     */
    public List<User> getAllPatients() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.PATIENT)
                .collect(Collectors.toList());
    }

    /**
     * Get all system appointments as DTOs to avoid serialization/lazy loading issues
     */
    public List<com.arundhati.clinic.dto.AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToAppointmentDTO)
                .collect(Collectors.toList());
    }

    private com.arundhati.clinic.dto.AppointmentDTO convertToAppointmentDTO(Appointment appointment) {
        if (appointment == null) return null;
        
        return com.arundhati.clinic.dto.AppointmentDTO.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient() != null ? appointment.getPatient().getName() : "Unknown Patient")
                .doctorName(appointment.getSlot() != null && appointment.getSlot().getDoctor() != null ? appointment.getSlot().getDoctor().getName() : "Unknown Doctor")
                .appointmentStart(appointment.getSlot() != null ? appointment.getSlot().getStartTime() : null)
                .status(appointment.getStatus())
                .amountPaid(appointment.getAmountPaid())
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    /**
     * Delete a user by ID (Doctor or Patient)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        // If doctor, delete profile first
        if (user.getRole() == Role.DOCTOR) {
            doctorProfileRepository.findByUserId(userId).ifPresent(doctorProfileRepository::delete);
        }
        
        userRepository.delete(user);
    }

    /**
     * Delete an appointment by ID
     */
    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException("Appointment not found", HttpStatus.NOT_FOUND));
        
        // Release slot if not completed
        if (appointment.getSlot() != null) {
            appointment.getSlot().setBooked(false);
            // No need to delete slot, just release it
        }
        
        appointmentRepository.delete(appointment);
    }

    /**
     * Get count of pending doctor verifications
     * @return count of unverified doctors
     */
    @Transactional
    public long getPendingDoctorCount() {
        return doctorProfileRepository.countByVerifiedFalse();
    }

    /**
     * Get system analytics with pending doctors count
     */
    public AdminAnalyticsDTO getAnalytics() {
        log.info("Generating admin analytics");
        
        List<Appointment> allAppointments = appointmentRepository.findAll();
        long totalPatients = userRepository.findAll().stream().filter(u -> u.getRole() == Role.PATIENT).count();
        long totalDoctors = userRepository.findAll().stream().filter(u -> u.getRole() == Role.DOCTOR).count();
        long pendingDoctorsCount = getPendingDoctorCount();
        
        Double totalRev = auditLogRepository.getTotalRevenue();
        if (totalRev == null) totalRev = 0.0;

        Map<String, Long> byStatus = allAppointments.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));

        Map<String, Long> bySpecialty = allAppointments.stream()
                .collect(Collectors.groupingBy(a -> a.getSlot().getDoctor().getName(), Collectors.counting()));

        Map<String, Double> revByDoctor = allAppointments.stream()
                .filter(a -> a.getAmountPaid() != null && a.getAmountPaid() > 0)
                .collect(Collectors.groupingBy(
                        a -> a.getSlot().getDoctor().getName(),
                        Collectors.summingDouble(Appointment::getAmountPaid)
                ));

        log.info("Analytics generated - Pending doctors: {}", pendingDoctorsCount);

        return AdminAnalyticsDTO.builder()
                .totalRevenue(totalRev)
                .totalAppointments(allAppointments.size())
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .pendingDoctorVerifications(pendingDoctorsCount)
                .appointmentsByStatus(byStatus)
                .appointmentsBySpecialty(bySpecialty)
                .revenueByDoctor(revByDoctor)
                .build();
    }
}
