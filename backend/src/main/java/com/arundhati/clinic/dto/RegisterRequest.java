package com.arundhati.clinic.dto;

import com.arundhati.clinic.entity.ConsultationMode;
import com.arundhati.clinic.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name must only contain letters and spaces")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 10, message = "Phone number must be exactly 10 digits")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must contain only numbers")
    private String phone;
    
    @NotNull(message = "Role is required (PATIENT or DOCTOR)")
    private Role role;

    // Doctor specific fields:
    private String specialty;
    private ConsultationMode mode;

    @Min(value = 50, message = "Minimum consultation fee is 50")
    private Double fees;
}
