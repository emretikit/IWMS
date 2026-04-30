package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.ChangePasswordRequest;
import com.hacettepe.iwms.dto.ProfileResponseDto;
import com.hacettepe.iwms.dto.SupervisorCompanyProfileUpdateRequest;
import com.hacettepe.iwms.entity.Company;
import com.hacettepe.iwms.entity.InternshipSupervisor;
import com.hacettepe.iwms.entity.Role;
import com.hacettepe.iwms.entity.Student;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.CompanyRepository;
import com.hacettepe.iwms.repository.InternshipSupervisorRepository;
import com.hacettepe.iwms.repository.StudentRepository;
import com.hacettepe.iwms.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InternshipSupervisorRepository internshipSupervisorRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> getProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = getUser(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully.", buildProfile(user)));
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> updatePassword(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        User user = getUser(currentUser.getId());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password updated successfully.", null));
    }

    @PutMapping("/supervisor-company")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> updateSupervisorCompany(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                                                   @Valid @RequestBody SupervisorCompanyProfileUpdateRequest request) {
        User user = getUser(currentUser.getId());
        InternshipSupervisor supervisor = internshipSupervisorRepository.findByCompanyEmailIgnoreCase(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor profile not found."));
        Company company = supervisor.getCompany();
        if (company == null) {
            throw new ResourceNotFoundException("Company profile not found.");
        }

        company.setName(request.getCompanyName().trim());
        company.setAddress(request.getCompanyAddress().trim());
        companyRepository.save(company);

        return ResponseEntity.ok(new ApiResponse<>(true, "Company profile updated successfully.", buildProfile(user)));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private ProfileResponseDto buildProfile(User user) {
        ProfileResponseDto.ProfileResponseDtoBuilder builder = ProfileResponseDto.builder()
                .role(user.getRole())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname());

        if (user.getRole() == Role.STUDENT) {
            Student student = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
            builder.currentYear(student.getCurrentYear())
                    .department(student.getDepartment());
        }

        if (user.getRole() == Role.SUPERVISOR && StringUtils.hasText(user.getEmail())) {
            internshipSupervisorRepository.findByCompanyEmailIgnoreCase(user.getEmail()).ifPresent(supervisor -> {
                builder.title(supervisor.getTitle())
                        .engineerType(supervisor.getEngineerType());
                if (supervisor.getCompany() != null) {
                    builder.companyName(supervisor.getCompany().getName())
                            .companyAddress(supervisor.getCompany().getAddress());
                }
            });
        }

        return builder.build();
    }
}
