package com.arundhati.clinic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookAppointmentRequest {
    @NotNull(message = "Slot ID is required")
    private Long slotId;
    
    // Simulating payment info here if needed
    private Double amountPaid;
}
