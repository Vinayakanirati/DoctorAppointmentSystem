package com.arundhati.clinic.controller;

import com.arundhati.clinic.dto.ProfileDTO;
import com.arundhati.clinic.entity.Role;
import com.arundhati.clinic.entity.User;
import com.arundhati.clinic.exception.BusinessException;
import com.arundhati.clinic.repository.DoctorProfileRepository;
import com.arundhati.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        ProfileDTO.ProfileDTOBuilder builder = ProfileDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole());

        if (user.getRole() == Role.DOCTOR) {
            doctorProfileRepository.findByUserId(user.getId()).ifPresent(p -> {
                builder.specialty(p.getSpecialty())
                        .mode(p.getMode())
                        .fees(p.getFees())
                        .isVerified(p.isVerified());
            });
        }

        return ResponseEntity.ok(builder.build());
    }

    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(@RequestBody ProfileDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        if (user.getRole() == Role.DOCTOR) {
            doctorProfileRepository.findByUserId(user.getId()).ifPresent(p -> {
                if (request.getSpecialty() != null) p.setSpecialty(request.getSpecialty());
                if (request.getMode() != null) p.setMode(request.getMode());
                if (request.getFees() != null) p.setFees(request.getFees());
                if (p != null) doctorProfileRepository.save(p);
            });
        }

        return getProfile();
    }
}
