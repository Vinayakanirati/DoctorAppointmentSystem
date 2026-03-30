package com.arundhati.clinic.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class AdminAnalyticsDTO {
    private double totalRevenue;
    private long totalAppointments;
    private long totalPatients;
    private long totalDoctors;
    private long pendingDoctorVerifications;
    
    // Data for charts
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> appointmentsBySpecialty;
    private Map<String, Double> revenueByDoctor;
}
