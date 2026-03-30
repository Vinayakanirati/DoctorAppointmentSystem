package com.arundhati.clinic.config;

import com.arundhati.clinic.entity.ConsultationMode;
import com.arundhati.clinic.entity.DoctorProfile;
import com.arundhati.clinic.entity.Role;
import com.arundhati.clinic.entity.Slot;
import com.arundhati.clinic.entity.User;
import com.arundhati.clinic.repository.DoctorProfileRepository;
import com.arundhati.clinic.repository.SlotRepository;
import com.arundhati.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create Admin
        if (!userRepository.existsByEmail("vivekperla333@gmail.com")) {
            User admin = new User();
            admin.setName("Default Admin");
            admin.setEmail("vivekperla333@gmail.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Role.ADMIN);
            admin.setPhone("0000000000");
            userRepository.save(admin);
            System.out.println("✓ Default Admin initialized with email 'root' and password '1234'");
        }

        // Create Test Doctors
        createTestDoctor("dr_cardio@clinic.com", "Dr. Rajesh Kumar", "Cardiology", 150.0, ConsultationMode.ONLINE);
        createTestDoctor("dr_neuro@clinic.com", "Dr. Priya Sharma", "Neurology", 120.0, ConsultationMode.OFFLINE);
        createTestDoctor("dr_ortho@clinic.com", "Dr. Akshay Patel", "Orthopedics", 100.0, ConsultationMode.ONLINE);
        createTestDoctor("dr_derm@clinic.com", "Dr. Sneha Singh", "Dermatology", 90.0, ConsultationMode.OFFLINE);
        createTestDoctor("dr_pedi@clinic.com", "Dr. Vikram Desai", "Pediatrics", 80.0, ConsultationMode.ONLINE);
    }

    private void createTestDoctor(String email, String name, String specialty, Double fees, ConsultationMode mode) {
        if (!userRepository.existsByEmail(email)) {
            // Create user
            User doctor = new User();
            doctor.setName(name);
            doctor.setEmail(email);
            doctor.setPassword(passwordEncoder.encode("1234"));
            doctor.setRole(Role.DOCTOR);
            doctor.setPhone("9999999999");
            User savedDoctor = userRepository.save(doctor);

            // Create doctor profile (AUTO-VERIFIED for testing)
            DoctorProfile profile = new DoctorProfile();
            profile.setUser(savedDoctor);
            profile.setSpecialty(specialty);
            profile.setFees(fees);
            profile.setMode(mode);
            profile.setVerified(true); // VERIFIED so patients can see them
            doctorProfileRepository.save(profile);

            // Create test slots for the next 7 days (half-hour intervals, 8 AM - 5 PM)
            createTestSlots(savedDoctor);

            System.out.println("✓ Test Doctor created: " + name + " (" + specialty + ") - Email: " + email);
        }
    }

    private void createTestSlots(User doctor) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.withHour(8).withMinute(0).withSecond(0).withNano(0);
        if (now.isAfter(today.withHour(17))) {
            today = today.plusDays(1);
        }

        // Create slots for next 7 days
        for (int day = 0; day < 7; day++) {
            LocalDateTime dayStart = today.plusDays(day);

            // Create 30-minute slots from 8 AM to 5 PM, excluding 1-2 PM lunch
            for (int hour = 8; hour < 17; hour++) {
                // Skip lunch hour (1 PM - 2 PM, hour 13)
                if (hour == 13)
                    continue;

                // Create two 30-minute slots per hour
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalDateTime slotStart = dayStart.withHour(hour).withMinute(minute);
                    LocalDateTime slotEnd = slotStart.plusMinutes(30);

                    Slot slot = new Slot();
                    slot.setDoctor(doctor);
                    slot.setStartTime(slotStart);
                    slot.setEndTime(slotEnd);
                    slot.setBooked(false);

                    slotRepository.save(slot);
                }
            }
        }

        System.out.println("  ✓ Created test slots for Dr. " + doctor.getName());
    }
}
