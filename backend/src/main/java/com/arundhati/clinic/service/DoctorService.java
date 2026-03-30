package com.arundhati.clinic.service;

import com.arundhati.clinic.dto.DashboardStats;
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

    @Transactional
    public Slot createSlot(String email, SlotRequest request) {
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

        return slotRepository.save(slot);
    }

    public List<Slot> getMySlots(String email) {
        User doctor = getDoctorUser(email);
        return slotRepository.findByDoctorId(doctor.getId());
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
        slotRepository.delete(slot);
    }

    public List<Appointment> getMyDailyAppointments(String email, LocalDate date) {
        User doctor = getDoctorUser(email);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return appointmentRepository.findBySlotDoctorIdAndSlotStartTimeBetween(doctor.getId(), start, end);
    }

    @Transactional
    public Appointment updateAppointmentStatus(String email, Long appointmentId, AppointmentStatus newStatus) {
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

        return appointment;
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
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED && a.getSlot().getStartTime().toLocalDate().equals(today))
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
