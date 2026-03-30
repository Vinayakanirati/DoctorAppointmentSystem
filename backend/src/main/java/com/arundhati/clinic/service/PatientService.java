package com.arundhati.clinic.service;

import com.arundhati.clinic.dto.BookAppointmentRequest;
import com.arundhati.clinic.dto.DoctorDTO;
import com.arundhati.clinic.entity.*;
import com.arundhati.clinic.exception.BusinessException;
import com.arundhati.clinic.repository.AppointmentRepository;
import com.arundhati.clinic.repository.AuditLogRepository;
import com.arundhati.clinic.repository.DoctorProfileRepository;
import com.arundhati.clinic.repository.SlotRepository;
import com.arundhati.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final AuditLogRepository auditLogRepository;

    private User getPatientUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Patient not found", HttpStatus.NOT_FOUND));
    }

    public List<DoctorDTO> browseDoctors(String specialty, ConsultationMode mode) {
        return doctorProfileRepository.findAll().stream()
                .filter(DoctorProfile::isVerified)
                .filter(p -> specialty == null || p.getSpecialty().equalsIgnoreCase(specialty))
                .filter(p -> mode == null || p.getMode() == mode)
                .map(p -> DoctorDTO.builder()
                        .id(p.getUser().getId())
                        .name(p.getUser().getName())
                        .email(p.getUser().getEmail())
                        .specialty(p.getSpecialty())
                        .mode(p.getMode())
                        .fees(p.getFees())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Slot> getDoctorAvailableSlots(Long doctorId) {
        return slotRepository.findByDoctorIdAndIsBookedFalse(doctorId);
    }

    @Transactional
    public Appointment bookAppointment(String email, BookAppointmentRequest request) {
        User patient = getPatientUser(email);
        
        try {
            Slot slot = slotRepository.findById(request.getSlotId())
                    .orElseThrow(() -> new BusinessException("Slot not found", HttpStatus.NOT_FOUND));

            if (slot.isBooked()) {
                throw new BusinessException("Slot is already booked", HttpStatus.CONFLICT);
            }

            DoctorProfile doctorProfile = doctorProfileRepository.findByUserId(slot.getDoctor().getId())
                    .orElseThrow(() -> new BusinessException("Doctor profile missing", HttpStatus.NOT_FOUND));

            // Optimistic Lock execution
            slot.setBooked(true);
            slot = slotRepository.save(slot);

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setSlot(slot);
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointment.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : doctorProfile.getFees());

            if (doctorProfile.getMode() == ConsultationMode.ONLINE) {
                String meetLink = "https://meet.google.com/" + UUID.randomUUID().toString().substring(0, 12);
                appointment.setMeetingLink(meetLink);
            } else {
                appointment.setClinicAddress("Arundhati Medical Clinic, Main Building, Suite A");
            }

            appointment = appointmentRepository.save(appointment);

            // Audit Log
            AuditLog audit = new AuditLog();
            audit.setEntityName("APPOINTMENT");
            audit.setEntityId(appointment.getId());
            audit.setAction("APPOINTMENT_BOOKED");
            audit.setAmount(appointment.getAmountPaid());
            audit.setPerformedBy(email);
            auditLogRepository.save(audit);
            
            // Payment Received Audit
            if (appointment.getAmountPaid() > 0) {
                AuditLog paymentAudit = new AuditLog();
                paymentAudit.setEntityName("PAYMENT");
                paymentAudit.setEntityId(appointment.getId());
                paymentAudit.setAction("PAYMENT_RECEIVED");
                paymentAudit.setAmount(appointment.getAmountPaid());
                paymentAudit.setPerformedBy(slot.getDoctor().getEmail()); // associate revenue with doctor
                auditLogRepository.save(paymentAudit);
            }

            // Emails
            String emailBody = String.format("Dear %s,\n\nYour appointment with Dr. %s is confirmed for %s.\n%s", 
                    patient.getName(), 
                    slot.getDoctor().getName(), 
                    slot.getStartTime().toString(),
                    doctorProfile.getMode() == ConsultationMode.ONLINE ? "Meet Link: " + appointment.getMeetingLink() : "Address: " + appointment.getClinicAddress()
            );
            emailService.sendEmail(patient.getEmail(), "Appointment Confirmed - Arundhati Clinic", emailBody);
            
            String doctorEmailBody = String.format("Dear Dr. %s,\n\nNew appointment booked by %s for %s.",
                    slot.getDoctor().getName(),
                    patient.getName(),
                    slot.getStartTime().toString()
            );
            emailService.sendEmail(slot.getDoctor().getEmail(), "New Appointment Booked", doctorEmailBody);

            return appointment;

        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new BusinessException("Slot was just booked by someone else. Please try another slot.", HttpStatus.CONFLICT);
        }
    }

    public List<Appointment> getMyHistory(String email) {
        User patient = getPatientUser(email);
        return appointmentRepository.findByPatientId(patient.getId());
    }

    @Transactional
    public void cancelAppointment(String email, Long appointmentId) {
        User patient = getPatientUser(email);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException("Appointment not found", HttpStatus.NOT_FOUND));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Can only cancel scheduled or confirmed appointments", HttpStatus.BAD_REQUEST);
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.getSlot().setBooked(false);
        
        slotRepository.save(appointment.getSlot());
        appointmentRepository.save(appointment);

        // Audit Log
        AuditLog audit = new AuditLog();
        audit.setEntityName("APPOINTMENT");
        audit.setEntityId(appointment.getId());
        audit.setAction("STATUS_CHANGED_TO_CANCELLED");
        audit.setAmount(0.0);
        audit.setPerformedBy(email);
        auditLogRepository.save(audit);

        // Send Email
        emailService.sendEmail(patient.getEmail(), "Appointment Cancelled", "Your appointment has been cancelled successfully as requested.");
        emailService.sendEmail(appointment.getSlot().getDoctor().getEmail(), "Appointment Cancelled", "Appointment with patient " + patient.getName() + " has been cancelled.");
    }
}
