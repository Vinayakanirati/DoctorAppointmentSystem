package com.arundhati.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalAppointmentsToday;
    private long pendingAppointments;
    private double totalEarnings;
    private double todayEarnings;
}
