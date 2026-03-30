package com.arundhati.clinic.service;

import com.arundhati.clinic.dto.AuthRequest;
import com.arundhati.clinic.dto.AuthResponse;
import com.arundhati.clinic.dto.RegisterRequest;
import com.arundhati.clinic.entity.Role;
import com.arundhati.clinic.entity.User;
import com.arundhati.clinic.repository.UserRepository;
import com.arundhati.clinic.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterPatientSuccessfully() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test Patient");
        req.setEmail("test@patient.com");
        req.setPassword("pass123");
        req.setRole(Role.PATIENT);
        req.setPhone("1234567890");

        when(userRepository.existsByEmail("test@patient.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = (User) i.getArguments()[0];
            u.setId(1L);
            return u;
        });

        String res = authService.register(req);

        assertNotNull(res);
        assertTrue(res.contains("OTP sent successfully"));
    }

    @Test
    void testLoginSuccessfully() {
        AuthRequest req = new AuthRequest("test@test.com", "pass123");
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockUser.setRole(Role.ADMIN);
        mockUser.setVerified(true);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(mockUser)).thenReturn("mock-jwt-token");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

        AuthResponse res = authService.login(req);

        assertEquals("mock-jwt-token", res.getToken());
        assertEquals(Role.ADMIN, res.getRole());
    }
}
