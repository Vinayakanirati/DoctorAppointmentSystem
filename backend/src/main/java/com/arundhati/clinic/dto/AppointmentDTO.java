package com.arundhati.clinic.dto;

import com.arundhati.clinic.entity.AppointmentStatus;
import com.arundhati.clinic.entity.ConsultationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private Long id;
    private String patientName;
    private String doctorName;
    private String doctorSpecialty;
    private ConsultationMode consultationMode;
    private LocalDateTime appointmentStart;
    private LocalDateTime appointmentEnd;
    private AppointmentStatus status;
    private Double amountPaid;
    private String meetingLink;
    private String clinicAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
