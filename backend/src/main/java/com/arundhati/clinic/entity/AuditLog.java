package com.arundhati.clinic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityName;
    private Long entityId;
    
    // E.g. "STATUS_CHANGE_TO_COMPLETED", "APPOINTMENT_BOOKED"
    private String action;
    
    // For revenue tracking
    private Double amount;
    
    // Email of the user who performed the action
    private String performedBy;

    private LocalDateTime timestamp = LocalDateTime.now();
}
