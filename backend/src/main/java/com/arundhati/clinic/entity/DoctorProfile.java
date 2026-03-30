package com.arundhati.clinic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String specialty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultationMode mode; // ONLINE or OFFLINE

    private Double fees = 0.0;

    @Column(name = "is_verified")
    private Boolean verified = false;

    public boolean isVerified() {
        return verified != null && verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
