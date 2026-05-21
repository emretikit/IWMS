package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.CompanyEvaluationRequest;
import com.hacettepe.iwms.entity.CompanyEvaluation;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.InternshipStatus;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.CompanyEvaluationRepository;
import com.hacettepe.iwms.repository.InternshipRepository;
import com.hacettepe.iwms.repository.UserRepository;
import com.hacettepe.iwms.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/company-evaluations")
@RequiredArgsConstructor
public class CompanyEvaluationController {
    private final InternshipRepository internshipRepository;
    private final CompanyEvaluationRepository companyEvaluationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @PostMapping("/{internshipId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<CompanyEvaluation>> submitEvaluation(@PathVariable Long internshipId,
                                                                           @Valid @RequestBody CompanyEvaluationRequest request,
                                                                           @AuthenticationPrincipal CustomUserDetails currentUser) throws Exception {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));
        if (internship.getStatus() != InternshipStatus.PENDING_COORDINATOR_REVIEW) {
            throw new ValidationException("Company evaluation can only be submitted after report submission.");
        }
        if (internship.getSupervisor() == null || !StringUtils.hasText(internship.getSupervisor().getCompanyEmail())) {
            throw new ValidationException("No valid supervisor is assigned to this internship.");
        }
        String loggedInEmail = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor user not found"))
                .getEmail();
        if (!StringUtils.hasText(loggedInEmail)
                || !loggedInEmail.equalsIgnoreCase(internship.getSupervisor().getCompanyEmail())) {
            throw new ValidationException("You are not authorized to submit company evaluation for this internship.");
        }
        String signatureHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(request.getSignatureFilePath().getBytes(StandardCharsets.UTF_8)));

        CompanyEvaluation evaluation = companyEvaluationRepository.findByInternshipId(internshipId).orElse(new CompanyEvaluation());
        evaluation.setInternship(internship);
        evaluation.setInternshipResultDocument(request.getInternshipResultDocument());
        evaluation.setReportEvaluationDocument(request.getReportEvaluationDocument());
        evaluation.setSignatureFilePath(request.getSignatureFilePath());
        evaluation.setSignatureSha256(signatureHash);
        evaluation.setSubmittedAt(LocalDateTime.now());
        CompanyEvaluation saved = companyEvaluationRepository.save(evaluation);

        internship.setStatus(InternshipStatus.EVALUATED_BY_COMPANY);
        internshipRepository.save(internship);
        auditService.log(currentUser.getId(), "COMPANY_EVALUATION_SUBMIT", "COMPANY_EVALUATION", saved.getId(), "submitted");
        return new ResponseEntity<>(new ApiResponse<>(true, "Company evaluation submitted.", saved), HttpStatus.CREATED);
    }
}
