package com.arundhati.clinic.service;

import com.arundhati.clinic.dto.AppointmentDTO;
import com.arundhati.clinic.dto.BookAppointmentRequest;
import com.arundhati.clinic.entity.Appointment;
import com.arundhati.clinic.entity.DoctorProfile;
import com.arundhati.clinic.entity.Role;
import com.arundhati.clinic.entity.ConsultationMode;
import com.arundhati.clinic.entity.Slot;
import com.arundhati.clinic.entity.User;
import com.arundhati.clinic.exception.BusinessException;
import com.arundhati.clinic.repository.AppointmentRepository;
import com.arundhati.clinic.repository.AuditLogRepository;
import com.arundhati.clinic.repository.DoctorProfileRepository;
import com.arundhati.clinic.repository.SlotRepository;
import com.arundhati.clinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PatientServiceTest {

    @Mock
    private SlotRepository slotRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DoctorProfileRepository doctorProfileRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("patient@test.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testBookAppointmentFailsIfSlotAlreadyBooked() {
        Slot mockSlot = new Slot();
        mockSlot.setId(10L);
        mockSlot.setBooked(true);

        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(new User()));
        when(slotRepository.findById(10L)).thenReturn(Optional.of(mockSlot));

        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setSlotId(10L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            patientService.bookAppointment("patient@test.com", req);
        });

        assertEquals("Slot is already booked", exception.getMessage());
    }

    @Test
    void testBookAppointmentSucceeds() {
        User patient = new User();
        patient.setEmail("patient@test.com");
        
        User doctorUser = new User();
        doctorUser.setId(5L);
        doctorUser.setName("Dr. Smith");
        doctorUser.setEmail("doc@test.com");

        DoctorProfile docProfile = new DoctorProfile();
        docProfile.setVerified(true);
        docProfile.setFees(100.0);
        docProfile.setMode(ConsultationMode.ONLINE);

        Slot mockSlot = new Slot();
        mockSlot.setId(10L);
        mockSlot.setBooked(false);
        mockSlot.setDoctor(doctorUser);
        mockSlot.setStartTime(LocalDateTime.now().plusDays(1));
        mockSlot.setEndTime(LocalDateTime.now().plusDays(1).plusMinutes(30));

        Appointment mockAppointment = new Appointment();
        mockAppointment.setId(1L);
        mockAppointment.setPatient(patient);
        mockAppointment.setSlot(mockSlot);
        mockAppointment.setAmountPaid(100.0);

        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(patient));
        when(slotRepository.findById(10L)).thenReturn(Optional.of(mockSlot));
        when(doctorProfileRepository.findByUserId(5L)).thenReturn(Optional.of(docProfile));
        when(slotRepository.save(any(Slot.class))).thenAnswer(i -> i.getArguments()[0]);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setSlotId(10L);

        AppointmentDTO result = patientService.bookAppointment("patient@test.com", req);

        assertNotNull(result);
        assertTrue(mockSlot.isBooked());
        assertEquals(100.0, result.getAmountPaid());
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString()); // 1 patient, 1 doctor
    }
}
