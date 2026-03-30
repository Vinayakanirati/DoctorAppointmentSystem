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

    private final EmailService emailService;

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT);
        }

        if (request.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot register as ADMIN directly", HttpStatus.FORBIDDEN);
        }

        // Generate 4-digit OTP
        String otp = String.valueOf(1000 + new java.util.Random().nextInt(9000));
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setVerified(false);
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));

        user = userRepository.save(user);

        if (request.getRole() == Role.DOCTOR) {
            DoctorProfile profile = new DoctorProfile();
            profile.setUser(user);
            profile.setSpecialty(request.getSpecialty());
            profile.setMode(request.getMode());
            profile.setFees(request.getFees() != null ? request.getFees() : 50.0);
            profile.setVerified(false);
            doctorProfileRepository.save(profile);
        }

        try {
            emailService.sendEmail(user.getEmail(), "Account Verification OTP - Arundhati Clinic", 
                "Your account verification OTP is: " + otp + "\nValid for 10 minutes.");
        } catch (Exception e) {
            // Keep registration but warn
        }

        return "OTP sent successfully to " + user.getEmail();
    }

    @Transactional
    public AuthResponse verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        
        if (user.isVerified()) {
            throw new BusinessException("Account is already verified", HttpStatus.BAD_REQUEST);
        }
        
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new BusinessException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }
        
        if (user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("OTP expired", HttpStatus.BAD_REQUEST);
        }
        
        user.setVerified(true);
        user.setOtp(null);
        userRepository.save(user);
        
        String jwtToken = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .message("Verification successful")
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (!user.isVerified()) {
            throw new BusinessException("Please verify your email first. An OTP was sent during registration.", HttpStatus.FORBIDDEN);
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String jwtToken = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }

    @Transactional
    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (user.isVerified()) {
            throw new BusinessException("Account is already verified", HttpStatus.BAD_REQUEST);
        }

        String otp = String.valueOf(1000 + new java.util.Random().nextInt(9000));
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        try {
            emailService.sendEmail(user.getEmail(), "Account Verification OTP - Arundhati Clinic",
                "Your account verification OTP is: " + otp + "\nValid for 10 minutes.");
        } catch (Exception e) {
            // Ignore email error for resend
        }

        return "New OTP sent to " + email;
    }
}
