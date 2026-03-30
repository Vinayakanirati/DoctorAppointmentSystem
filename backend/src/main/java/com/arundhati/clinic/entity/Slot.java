package com.arundhati.clinic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // Using User entity since it acts as the doctor

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_booked")
    private Boolean booked = false;

    @Column(name = "is_deleted")
    private Boolean deleted = false;

    public boolean isBooked() {
        return booked != null && booked;
    }

    public boolean isDeleted() {
        return deleted != null && deleted;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // Optimistic locking for concurrency
    @Version
    private Long version;
}
