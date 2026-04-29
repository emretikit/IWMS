package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.InternshipApplicationRequest;
import com.hacettepe.iwms.dto.InternshipReportDto;
import com.hacettepe.iwms.dto.InternshipReportSubmitRequest;
import com.hacettepe.iwms.dto.InternshipResponseDto;
import com.hacettepe.iwms.dto.ReportDraftRequest;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.InternshipReport;
import com.hacettepe.iwms.entity.InternshipStatus;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.InternshipRepository;
import com.hacettepe.iwms.service.IInternshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
@Slf4j
public class InternshipController {

    private final IInternshipService internshipService;
    private final InternshipRepository internshipRepository;
    @Value("${app.allow-test-endpoints:false}")
    private boolean allowTestEndpoints;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<InternshipResponseDto>> applyForInternship(
            @Valid @RequestBody InternshipApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("Received internship application request from user ID: {}", currentUser.getId());
        
        InternshipResponseDto responseDto = internshipService.applyForInternship(request, currentUser.getId());
        return new ResponseEntity<>(new ApiResponse<>(true, "Internship application submitted successfully.", responseDto), HttpStatus.CREATED);
    }

    @PostMapping("/{internshipId}/report")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<InternshipReportDto>> submitReport(
            @PathVariable Long internshipId,
            @Valid @RequestPart("report") InternshipReportSubmitRequest request,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        InternshipReportDto responseDto = internshipService.submitReport(internshipId, request, file, currentUser.getId());
        return new ResponseEntity<>(new ApiResponse<>(true, "Internship report submitted successfully.", responseDto), HttpStatus.OK);
    }

    @PostMapping("/{internshipId}/report/draft")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<InternshipReport>> saveReportDraft(
            @PathVariable Long internshipId,
            @Valid @RequestBody ReportDraftRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        InternshipReport report = internshipService.saveReportDraft(internshipId, currentUser.getId(), request.getTemplateContent());
        return new ResponseEntity<>(new ApiResponse<>(true, "Internship report draft saved.", report), HttpStatus.OK);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<InternshipResponseDto>>> getMyInternships(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<InternshipResponseDto> internships = internshipService.getStudentInternships(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Student internships retrieved successfully.", internships));
    }

    @PutMapping("/{internshipId}/approve")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<InternshipResponseDto>> approveBySupervisor(@PathVariable Long internshipId,
                                                                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        InternshipResponseDto responseDto = internshipService.approveBySupervisor(internshipId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Internship approved successfully.", responseDto));
    }

    @PutMapping("/{internshipId}/reject")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<InternshipResponseDto>> rejectBySupervisor(@PathVariable Long internshipId,
                                                                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        InternshipResponseDto responseDto = internshipService.rejectBySupervisor(internshipId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Internship rejected successfully.", responseDto));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<InternshipResponseDto>> getInternshipById(@PathVariable Long id) {
        InternshipResponseDto internship = internshipService.getInternshipById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Internship retrieved successfully.", internship));
    }

    // TEST AMAÇLI: Stajın durumunu "COMPLETED" yapmak için eklenen geçici uç nokta
    @PutMapping("/{internshipId}/mark-completed")
    public ResponseEntity<ApiResponse<String>> markInternshipAsCompleted(@PathVariable Long internshipId) {
        if (!allowTestEndpoints) {
            throw new ValidationException("Test endpoints are disabled in this environment.");
        }
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Staj bulunamadı"));
        
        internship.setStatus(InternshipStatus.COMPLETED);
        internshipRepository.save(internship);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Staj başarıyla COMPLETED durumuna alındı.", null));
    }
}
