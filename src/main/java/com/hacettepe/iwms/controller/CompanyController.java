package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.CompanyRegistrationRequest;
import com.hacettepe.iwms.dto.CompanyResponseDto;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.repository.UserRepository;
import com.hacettepe.iwms.service.ICompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final ICompanyService companyService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> registerCompany(@Valid @RequestBody CompanyRegistrationRequest request) {
        CompanyResponseDto responseDto = companyService.registerCompany(request);
        return new ResponseEntity<>(new ApiResponse<>(true, "Company registered successfully, awaiting admin approval.", responseDto), HttpStatus.CREATED);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CompanyResponseDto>>> getPendingCompanies() {
        List<CompanyResponseDto> companies = companyService.getPendingCompanies();
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending companies retrieved successfully.", companies));
    }

    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<CompanyResponseDto>>> getApprovedCompanies() {
        List<CompanyResponseDto> companies = companyService.getApprovedCompanies();
        return ResponseEntity.ok(new ApiResponse<>(true, "Approved companies retrieved successfully.", companies));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> approveCompany(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails adminDetails) {
        User adminUser = userRepository.findById(adminDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        CompanyResponseDto responseDto = companyService.approveCompany(id, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company approved successfully.", responseDto));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> rejectCompany(@PathVariable Long id, @RequestParam String reason, @AuthenticationPrincipal CustomUserDetails adminDetails) {
        User adminUser = userRepository.findById(adminDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        CompanyResponseDto responseDto = companyService.rejectCompany(id, adminUser, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company rejected successfully.", responseDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'COORDINATOR', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> getCompanyById(@PathVariable Long id) {
        CompanyResponseDto company = companyService.getCompanyById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company retrieved successfully.", company));
    }
}
