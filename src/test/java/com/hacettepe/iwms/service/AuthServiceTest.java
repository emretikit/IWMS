package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.AuthRequest;
import com.hacettepe.iwms.dto.RegisterRequest;
import com.hacettepe.iwms.entity.Role;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.PasswordResetTokenRepository;
import com.hacettepe.iwms.repository.StudentRepository;
import com.hacettepe.iwms.repository.UserRepository;
import com.hacettepe.iwms.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
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
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThrows(ValidationException.class, () -> authService.login(request));
    }
}
