package com.arundhati.clinic.service;

import com.arundhati.clinic.dto.AppointmentDTO;
import com.arundhati.clinic.dto.DashboardStats;
import com.arundhati.clinic.dto.SlotDTO;
import com.arundhati.clinic.dto.SlotRequest;
import com.arundhati.clinic.entity.*;
import com.arundhati.clinic.exception.BusinessException;
import com.arundhati.clinic.repository.AppointmentRepository;
import com.arundhati.clinic.repository.AuditLogRepository;
import com.arundhati.clinic.repository.DoctorProfileRepository;
import com.arundhati.clinic.repository.SlotRepository;
import com.arundhati.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    private User getDoctorUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Doctor not found", HttpStatus.NOT_FOUND));
    }

    private DoctorProfile getVerifiedProfile(Long userId) {
        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Doctor profile missing", HttpStatus.NOT_FOUND));
        if (!profile.isVerified()) {
            throw new BusinessException("Your profile is not verified by admin yet", HttpStatus.FORBIDDEN);
        }
        return profile;
    }

    private SlotDTO convertSlotToDTO(Slot slot) {
        return SlotDTO.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isBooked(slot.isBooked())
                .doctorId(slot.getDoctor().getId())
                .build();
    }

    private AppointmentDTO convertAppointmentToDTO(Appointment appointment) {
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient().getName())
                .doctorName(appointment.getSlot().getDoctor().getName())
                .consultationMode(doctorProfileRepository.findByUserId(appointment.getSlot().getDoctor().getId())
                        .map(DoctorProfile::getMode)
                        .orElse(ConsultationMode.ONLINE))
                .doctorSpecialty(doctorProfileRepository.findByUserId(appointment.getSlot().getDoctor().getId())
                        .map(DoctorProfile::getSpecialty)
                        .orElse("General"))
                .appointmentStart(appointment.getSlot().getStartTime())
                .appointmentEnd(appointment.getSlot().getEndTime())
                .status(appointment.getStatus())
                .amountPaid(appointment.getAmountPaid())
                .meetingLink(appointment.getMeetingLink())
                .clinicAddress(appointment.getClinicAddress())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    @Transactional
    public SlotDTO createSlot(String email, SlotRequest request) {
        User doctor = getDoctorUser(email);
        getVerifiedProfile(doctor.getId()); // ensure verified

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot create slot in the past", HttpStatus.BAD_REQUEST);
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException("Start time must be before end time", HttpStatus.BAD_REQUEST);
        }

        Slot slot = new Slot();
        slot.setDoctor(doctor);
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setBooked(false);

        slot = slotRepository.save(slot);
        return convertSlotToDTO(slot);
    }

    @Transactional
    public List<SlotDTO> getMySlots(String email) {
        User doctor = getDoctorUser(email);
        Long doctorId = doctor.getId();

        // Auto-Backfill slots for the next 7 days
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = now.toLocalDate().plusDays(i);
            
            // Working Hours: 8 AM to 5 PM (17:00)
            for (int hour = 8; hour < 17; hour++) {
                // Lunch Break: 1 PM to 2 PM (Skip hour 13)
                if (hour == 13) continue;

                LocalDateTime startTime = date.atTime(hour, 0);
                LocalDateTime endTime = startTime.plusHours(1);

                // Check if slot already exists (including deleted ones)
                java.util.Optional<Slot> existingSlot = slotRepository.findByDoctorIdAndStartTime(doctorId, startTime);
                if (existingSlot.isEmpty()) {
                    Slot newSlot = new Slot();
                    newSlot.setDoctor(doctor);
                    newSlot.setStartTime(startTime);
                    newSlot.setEndTime(endTime);
                    newSlot.setBooked(false);
                    newSlot.setDeleted(false);
                    slotRepository.save(newSlot);
                }
            }
        }

        return slotRepository.findByDoctorId(doctor.getId())
                .stream()
                .filter(slot -> !slot.isDeleted()) // Exclude deleted
                .filter(slot -> slot.getStartTime().isAfter(now)) // Exclude past slots ('timed out')
                .map(this::convertSlotToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSlot(String email, Long slotId) {
        User doctor = getDoctorUser(email);
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException("Slot not found", HttpStatus.NOT_FOUND));

        if (!slot.getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }
        if (slot.isBooked()) {
            throw new BusinessException("Cannot delete booked slot. Please cancel the appointment instead.", HttpStatus.BAD_REQUEST);
        }
        
        slot.setDeleted(true); // Logical delete
        slotRepository.save(slot);
    }

    public List<AppointmentDTO> getMyDailyAppointments(String email, LocalDate date) {
        User doctor = getDoctorUser(email);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return appointmentRepository.findBySlotDoctorIdAndSlotStartTimeBetween(doctor.getId(), start, end)
                .stream()
                .map(this::convertAppointmentToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDTO updateAppointmentStatus(String email, Long appointmentId, AppointmentStatus newStatus) {
        User doctor = getDoctorUser(email);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException("Appointment not found", HttpStatus.NOT_FOUND));

        if (!appointment.getSlot().getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(newStatus);
        
        // Handle slot release if cancelled
        if (newStatus == AppointmentStatus.CANCELLED || newStatus == AppointmentStatus.NO_SHOW) {
            appointment.getSlot().setBooked(false);
            slotRepository.save(appointment.getSlot());
        }

        appointment = appointmentRepository.save(appointment);

        // Audit Trail
        AuditLog audit = new AuditLog();
        audit.setEntityName("APPOINTMENT");
        audit.setEntityId(appointment.getId());
        audit.setAction("STATUS_CHANGED_TO_" + newStatus.name());
        audit.setPerformedBy(email);
        
        // Tracking revenue if completed
        if (newStatus == AppointmentStatus.COMPLETED && oldStatus != AppointmentStatus.COMPLETED) {
            audit.setAmount(appointment.getAmountPaid());
        } else {
            audit.setAmount(0.0);
        }
        auditLogRepository.save(audit);

        return convertAppointmentToDTO(appointment);
    }

    public DashboardStats getDashboardStats(String email) {
        User doctor = getDoctorUser(email);
        List<Appointment> allAppointments = appointmentRepository.findBySlotDoctorId(doctor.getId());
        
        LocalDate today = LocalDate.now();
        
        long totalToday = allAppointments.stream()
                .filter(a -> a.getSlot().getStartTime().toLocalDate().equals(today))
                .count();
                
        long pending = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .count();

        Double totalEarnings = auditLogRepository.getDoctorRevenue(email);
        if (totalEarnings == null) totalEarnings = 0.0;
        
        double todayEarnings = allAppointments.stream()
                .filter(a -> (a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.SCHEDULED) 
                        && a.getSlot().getStartTime().toLocalDate().equals(today))
                .mapToDouble(Appointment::getAmountPaid)
                .sum();

        return DashboardStats.builder()
                .totalAppointmentsToday(totalToday)
                .pendingAppointments(pending)
                .totalEarnings(totalEarnings)
                .todayEarnings(todayEarnings)
                .build();
    }
}
