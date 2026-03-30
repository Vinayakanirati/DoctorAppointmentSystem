package com.arundhati.clinic.dto;

import com.arundhati.clinic.entity.ConsultationMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorDTO {
    private Long id;
    private String name;
    private String email;
    private String specialty;
    private ConsultationMode mode;
    private Double fees;
}
