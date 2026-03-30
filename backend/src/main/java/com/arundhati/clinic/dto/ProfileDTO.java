package com.arundhati.clinic.dto;

import com.arundhati.clinic.entity.ConsultationMode;
import com.arundhati.clinic.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    
    // Doctor Specific
    private String specialty;
    private ConsultationMode mode;
    private Double fees;
    private Boolean isVerified;
}
