package com.arundhati.clinic.repository;

import com.arundhati.clinic.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);
    
    @Query("SELECT SUM(a.amount) FROM AuditLog a WHERE a.action = 'PAYMENT_RECEIVED'")
    Double getTotalRevenue();
    
    @Query("SELECT SUM(a.amount) FROM AuditLog a WHERE a.action = 'PAYMENT_RECEIVED' AND a.performedBy = :doctorEmail")
    Double getDoctorRevenue(String doctorEmail);
}
