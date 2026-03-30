package com.arundhati.clinic.service;

import com.arundhati.clinic.dto.AdminAnalyticsDTO;
import com.arundhati.clinic.entity.Appointment;
import com.arundhati.clinic.entity.DoctorProfile;
import com.arundhati.clinic.entity.Role;
import com.arundhati.clinic.exception.BusinessException;
import com.arundhati.clinic.repository.AppointmentRepository;
import com.arundhati.clinic.repository.AuditLogRepository;
import com.arundhati.clinic.repository.DoctorProfileRepository;
import com.arundhati.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;

    public List<DoctorProfile> getPendingDoctors() {
        return doctorProfileRepository.findAll()
                .stream()
                .filter(p -> !p.isVerified())
                .collect(Collectors.toList());
    }

    public DoctorProfile verifyDoctor(Long profileId) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profile not found", HttpStatus.NOT_FOUND));

        profile.setVerified(true);
        doctorProfileRepository.save(profile);

        emailService.sendEmail(
                profile.getUser().getEmail(),
                "Profile Verified - Arundhati Clinic",
                "Congratulations! Your doctor profile has been verified by the Admin. You can now login and create slots."
        );

        return profile;
    }

    public AdminAnalyticsDTO getAnalytics() {
        List<Appointment> allAppointments = appointmentRepository.findAll();
        long totalPatients = userRepository.findAll().stream().filter(u -> u.getRole() == Role.PATIENT).count();
        long totalDoctors = userRepository.findAll().stream().filter(u -> u.getRole() == Role.DOCTOR).count();
        long pendingDoctors = getPendingDoctors().size();
        
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

        return AdminAnalyticsDTO.builder()
                .totalRevenue(totalRev)
                .totalAppointments(allAppointments.size())
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .pendingDoctorVerifications(pendingDoctors)
                .appointmentsByStatus(byStatus)
                .appointmentsBySpecialty(bySpecialty)
                .revenueByDoctor(revByDoctor)
                .build();
    }
}
