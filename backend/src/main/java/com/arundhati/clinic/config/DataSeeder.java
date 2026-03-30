package com.arundhati.clinic.config;

import com.arundhati.clinic.entity.Role;
import com.arundhati.clinic.entity.User;
import com.arundhati.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("root")) {
            User admin = new User();
            admin.setName("Default Admin");
            admin.setEmail("root");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setRole(Role.ADMIN);
            admin.setPhone("0000000000");

            userRepository.save(admin);
            System.out.println("Default Admin initialized with email 'root' and password '1234'.");
        }
    }
}
