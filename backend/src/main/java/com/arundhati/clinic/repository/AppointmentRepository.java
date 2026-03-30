package com.arundhati.clinic.repository;

import com.arundhati.clinic.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findBySlotDoctorId(Long doctorId);
    List<Appointment> findBySlotDoctorIdAndSlotStartTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
}
