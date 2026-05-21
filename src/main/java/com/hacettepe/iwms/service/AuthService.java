package com.hacettepe.iwms.service;

import org.springframework.security.core.context.SecurityContextHolder;

import com.hacettepe.iwms.dto.AuthRequest;
import com.hacettepe.iwms.dto.AuthResponse;
import com.hacettepe.iwms.dto.PasswordUpdateRequest;
import com.hacettepe.iwms.dto.RegisterRequest;
import com.hacettepe.iwms.entity.ApprovalStatus;
import com.hacettepe.iwms.entity.InternshipSupervisor;
import com.hacettepe.iwms.entity.PasswordResetToken;
import com.hacettepe.iwms.entity.Role;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.InternshipSupervisorRepository;
import com.hacettepe.iwms.repository.PasswordResetTokenRepository;
import com.hacettepe.iwms.repository.StudentRepository;
import com.hacettepe.iwms.repository.UserRepository;
import com.hacettepe.iwms.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final InternshipSupervisorRepository internshipSupervisorRepository;
    private final AuditService auditService;

    @Value("${app.student-email-domain:cs.hacettepe.edu.tr}")
    private String studentEmailDomain;

    @Transactional
    public void register(RegisterRequest request) {
        InternshipSupervisor matchedSupervisor = null;

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ValidationException("Username is already taken!");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("Email is already in use!");
        }
        if (request.getRole() == Role.STUDENT) {
            if (!request.getEmail().toLowerCase().endsWith("@" + studentEmailDomain)) {
                throw new ValidationException("Students must register with @" + studentEmailDomain + " email.");
            }
            if (!StringUtils.hasText(request.getYear())) {
                throw new ValidationException("Student 'year' must be provided.");
            }
            if ("1".equals(request.getYear())) {
                throw new ValidationException("First-year students are not eligible for an internship.");
            }
        }
        if (request.getRole() == Role.SUPERVISOR) {
            matchedSupervisor = internshipSupervisorRepository.findByCompanyEmailIgnoreCase(request.getEmail())
                    .orElseThrow(() -> new ValidationException("Supervisor signup is only allowed with a company-registered supervisor email."));
            if (matchedSupervisor.getCompany() == null || matchedSupervisor.getCompany().getApprovalStatus() != ApprovalStatus.APPROVED) {
                throw new ValidationException("Supervisor can sign up only after the related company is approved.");
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .name(request.getName())
                .surname(request.getSurname())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        if (request.getRole() == Role.STUDENT) {
            Student student = new Student();
            student.setUser(savedUser);
            student.setStudentNumber("STU-" + UUID.randomUUID().toString().substring(0, 8));
            student.setCurrentYear(request.getYear());
            studentRepository.save(student);
        }

        if (request.getRole() == Role.SUPERVISOR && matchedSupervisor != null) {
            matchedSupervisor.setFirstName(request.getName());
            matchedSupervisor.setLastName(request.getSurname());
            matchedSupervisor.setTitle(request.getTitle());
            if (request.getEngineerType() != null) {
                matchedSupervisor.setEngineerType(request.getEngineerType());
            }
            internshipSupervisorRepository.save(matchedSupervisor);
        }
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            auditService.log(null, "LOGIN_FAILED", "USER", null, request.getUsername());
            throw new ValidationException("Incorrect login ID or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String token = jwtUtil.generateToken(userDetails);
        
        User user = userRepository.findByUsername(request.getUsername()).get();
        auditService.log(user.getId(), "LOGIN_SUCCESS", "USER", user.getId(), user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Transactional
    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email."));
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(token);
        auditService.log(user.getId(), "PASSWORD_RESET_REQUESTED", "USER", user.getId(), email);
        log.info("Password reset token for {} : {}", email, token.getToken());
        return token.getToken();
    }

    @Transactional
    public void resetPassword(PasswordUpdateRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ValidationException("Invalid password reset token."));
        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Password reset token is expired or already used.");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        token.setUsed(true);
        userRepository.save(user);
        passwordResetTokenRepository.save(token);
        auditService.log(user.getId(), "PASSWORD_RESET_COMPLETED", "USER", user.getId(), user.getEmail());
    }
}
