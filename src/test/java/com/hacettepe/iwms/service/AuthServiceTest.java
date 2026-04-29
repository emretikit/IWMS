package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.AuthRequest;
import com.hacettepe.iwms.dto.RegisterRequest;
import com.hacettepe.iwms.entity.Role;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.*;
import com.hacettepe.iwms.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AcademicAdvisorRepository academicAdvisorRepository;
    @Mock private InternshipCoordinatorRepository internshipCoordinatorRepository;
    @Mock private InternshipSupervisorRepository internshipSupervisorRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldRejectStudentWithInvalidEmailDomain() {
        ReflectionTestUtils.setField(authService, "studentEmailDomain", "cs.hacettepe.edu.tr");
        RegisterRequest request = new RegisterRequest();
        request.setUsername("stu1");
        request.setEmail("user@gmail.com");
        request.setPassword("secret123");
        request.setRole(Role.STUDENT);
        when(userRepository.findByUsername("stu1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldMapBadCredentialsToValidationException() {
        AuthRequest request = new AuthRequest();
        request.setUsername("wrong");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(ArgumentMatchers.any()))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThrows(ValidationException.class, () -> authService.login(request));
    }

    @Test
    void register_shouldCreateAcademicAdvisorWhenRoleIsAdvisor() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("advisor1");
        request.setEmail("advisor1@hacettepe.edu.tr");
        request.setPassword("pass123");
        request.setRole(Role.ACADEMIC_ADVISOR);
        request.setName("Name");
        request.setSurname("Surname");

        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        authService.register(request);

        verify(academicAdvisorRepository, times(1)).save(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void register_shouldCreateCoordinatorWhenRoleIsCoordinator() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("coord1");
        request.setEmail("coord1@hacettepe.edu.tr");
        request.setPassword("pass123");
        request.setRole(Role.COORDINATOR);
        request.setName("Name");
        request.setSurname("Surname");

        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        authService.register(request);

        verify(internshipCoordinatorRepository, times(1)).save(any());
    }

    @Test
    void register_shouldCreateSupervisorWhenRoleIsSupervisor() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sup1");
        request.setEmail("sup1@company.com");
        request.setPassword("pass123");
        request.setRole(Role.SUPERVISOR);
        request.setName("Name");
        request.setSurname("Surname");

        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        authService.register(request);

        verify(internshipSupervisorRepository, times(1)).save(any());
    }
}
