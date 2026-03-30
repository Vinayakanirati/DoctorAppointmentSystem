package com.arundhati.clinic.dto;

import com.arundhati.clinic.entity.ConsultationMode;
import com.arundhati.clinic.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String phone;
    
    @NotNull(message = "Role is required (PATIENT or DOCTOR)")
    private Role role;

    // Doctor specific fields:
    private String specialty;
    private ConsultationMode mode;
    private Double fees;
}
