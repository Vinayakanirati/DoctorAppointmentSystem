package com.arundhati.clinic.dto;

import com.arundhati.clinic.entity.ConsultationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingDoctorDTO {
    
    @NotNull(message = "Doctor profile ID cannot be null")
    private Long id;
    
    @NotNull(message = "Doctor name cannot be null")
    private String name;
    
    @NotNull(message = "Doctor email cannot be null")
    private String email;
    
    @NotNull(message = "Specialty cannot be null")
    private String specialty;
    
    @NotNull(message = "Consultation mode cannot be null")
    private ConsultationMode mode;
    
    private Double fees;
    
    private String phone;
    
    @NotNull(message = "Verification status cannot be null")
    private boolean verified;
    
    private String registrationTimeAgo; // Human-readable format
}
