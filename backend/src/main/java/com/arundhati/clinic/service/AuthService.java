package com.arundhati.clinic.service;

import com.arundhati.clinic.dto.AuthRequest;
import com.arundhati.clinic.dto.AuthResponse;
import com.arundhati.clinic.dto.RegisterRequest;
import com.arundhati.clinic.entity.DoctorProfile;
import com.arundhati.clinic.entity.Role;
import com.arundhati.clinic.entity.User;
import com.arundhati.clinic.exception.BusinessException;
import com.arundhati.clinic.repository.DoctorProfileRepository;
import com.arundhati.clinic.repository.UserRepository;
import com.arundhati.clinic.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT);
        }

        if (request.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot register as ADMIN directly", HttpStatus.FORBIDDEN);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());

        user = userRepository.save(user);

        if (request.getRole() == Role.DOCTOR) {
            if (request.getSpecialty() == null || request.getMode() == null) {
                throw new BusinessException("Specialty and Consultation Mode are required for Doctors", HttpStatus.BAD_REQUEST);
            }
            DoctorProfile profile = new DoctorProfile();
            profile.setUser(user);
            profile.setSpecialty(request.getSpecialty());
            profile.setMode(request.getMode());
            profile.setFees(request.getFees() != null ? request.getFees() : 0.0);
            profile.setVerified(false); // Default to unverified
            doctorProfileRepository.save(profile);
        }

        String jwtToken = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .message("User registered successfully. " + (user.getRole() == Role.DOCTOR ? "Waiting for admin verification." : ""))
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        String jwtToken = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }
}
