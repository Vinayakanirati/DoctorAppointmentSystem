package com.arundhati.clinic.repository;

import com.arundhati.clinic.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByDoctorIdAndStartTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Slot> findByDoctorId(Long doctorId);
    List<Slot> findByDoctorIdAndIsBookedFalse(Long doctorId);
    java.util.Optional<Slot> findByDoctorIdAndStartTime(Long doctorId, java.time.LocalDateTime startTime);
}
