package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.CoordinatorPendingInternshipDto;
import com.hacettepe.iwms.dto.CoordinatorDecisionRequest;
import com.hacettepe.iwms.entity.*;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.*;
import com.hacettepe.iwms.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/coordinator")
@RequiredArgsConstructor
public class CoordinatorController {
    private final InternshipRepository internshipRepository;
    private final InternshipReportRepository internshipReportRepository;
    private final InternshipCoordinatorRepository internshipCoordinatorRepository;
    private final CompanyEvaluationRepository companyEvaluationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationRepository notificationRepository;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<CoordinatorPendingInternshipDto>>> pendingSubmissions() {
        List<CoordinatorPendingInternshipDto> data = internshipRepository.findByStatusIn(Arrays.asList(
                InternshipStatus.PENDING_COORDINATOR_REVIEW,
                InternshipStatus.EVALUATED_BY_COMPANY
        )).stream().map(this::toPendingDto).toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending records listed.", data));
    }

    @PutMapping("/internships/{internshipId}/decision")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<ApiResponse<Void>> decide(@PathVariable Long internshipId,
                                                    @Valid @RequestBody CoordinatorDecisionRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));
        if (internship.getStatus() != InternshipStatus.PENDING_COORDINATOR_REVIEW
                && internship.getStatus() != InternshipStatus.EVALUATED_BY_COMPANY) {
            throw new ValidationException("Coordinator decision can only be made for internships pending coordinator review.");
        }
        InternshipCoordinator coordinator = internshipCoordinatorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Coordinator profile not found"));
        if (request.getStatus() != InternshipStatus.APPROVED
                && request.getStatus() != InternshipStatus.REJECTED
                && request.getStatus() != InternshipStatus.REVISION_REQUIRED) {
            throw new ValidationException("Invalid coordinator decision status.");
        }
        if (request.getStatus() == InternshipStatus.REVISION_REQUIRED && !StringUtils.hasText(request.getFeedback())) {
            throw new ValidationException("Feedback is required when revision is requested.");
        }
        internship.setCoordinator(coordinator);
        internship.setStatus(request.getStatus());
        Internship saved = internshipRepository.save(internship);

        if (request.getStatus() == InternshipStatus.REVISION_REQUIRED) {
            InternshipReport report = internshipReportRepository.findByInternshipId(saved.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Report not found for internship."));
            report.setDraft(true);
            report.setSubmissionStatus(SubmissionStatus.DRAFT);
            internshipReportRepository.save(report);
        }

        User studentUser = internship.getStudent().getUser();
        Notification notification = Notification.builder()
                .user(studentUser)
                .type("COORDINATOR_DECISION")
                .title("Internship decision: " + request.getStatus().name())
                .message(request.getFeedback())
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        auditService.log(currentUser.getId(), "COORDINATOR_DECISION", "INTERNSHIP", saved.getId(), LocalDateTime.now().toString());
        return ResponseEntity.ok(new ApiResponse<>(true, "Decision saved.", null));
    }

    private CoordinatorPendingInternshipDto toPendingDto(Internship internship) {
        InternshipReport report = internshipReportRepository.findByInternshipId(internship.getId()).orElse(null);
        CompanyEvaluation companyEvaluation = companyEvaluationRepository.findByInternshipId(internship.getId()).orElse(null);

        CoordinatorPendingInternshipDto.UserDto userDto = null;
        if (internship.getStudent() != null && internship.getStudent().getUser() != null) {
            User user = internship.getStudent().getUser();
            userDto = CoordinatorPendingInternshipDto.UserDto.builder()
                    .name(user.getName())
                    .surname(user.getSurname())
                    .email(user.getEmail())
                    .build();
        }

        CoordinatorPendingInternshipDto.StudentDto studentDto = null;
        if (internship.getStudent() != null) {
            Student student = internship.getStudent();
            studentDto = CoordinatorPendingInternshipDto.StudentDto.builder()
                    .id(student.getId())
                    .studentNumber(student.getStudentNumber())
                    .department(student.getDepartment())
                    .currentYear(student.getCurrentYear())
                    .user(userDto)
                    .build();
        }

        CoordinatorPendingInternshipDto.CompanyDto companyDto = null;
        if (internship.getCompany() != null) {
            companyDto = CoordinatorPendingInternshipDto.CompanyDto.builder()
                    .id(internship.getCompany().getId())
                    .name(internship.getCompany().getName())
                    .build();
        }

        CoordinatorPendingInternshipDto.AcademicPeriodDto academicPeriodDto = null;
        if (internship.getAcademicPeriod() != null) {
            AcademicPeriod period = internship.getAcademicPeriod();
            academicPeriodDto = CoordinatorPendingInternshipDto.AcademicPeriodDto.builder()
                    .id(period.getId())
                    .name(period.getName())
                    .submissionDeadline(period.getSubmissionDeadline())
                    .minInternshipDays(period.getMinInternshipDays())
                    .build();
        }

        CoordinatorPendingInternshipDto.ReportDto reportDto = null;
        if (report != null) {
            reportDto = CoordinatorPendingInternshipDto.ReportDto.builder()
                    .id(report.getId())
                    .submissionStatus(report.getSubmissionStatus() != null ? report.getSubmissionStatus().name() : null)
                    .draft(report.isDraft())
                    .submittedAt(report.getSubmittedAt())
                    .fileName(report.getFileName())
                    .templateContent(report.getTemplateContent())
                    .build();
        }

        CoordinatorPendingInternshipDto.EvaluationDto evaluationDto = null;
        if (companyEvaluation != null) {
            boolean documentsComplete = StringUtils.hasText(companyEvaluation.getSignatureFilePath());
            evaluationDto = CoordinatorPendingInternshipDto.EvaluationDto.builder()
                    .id(companyEvaluation.getId())
                    .result("SUBMITTED")
                    .feedback(null)
                    .documentsComplete(documentsComplete)
                    .rulesCompliant(documentsComplete)
                    .build();
        }

        return CoordinatorPendingInternshipDto.builder()
                .id(internship.getId())
                .status(internship.getStatus())
                .startDate(internship.getStartDate())
                .endDate(internship.getEndDate())
                .totalWorkingDays(internship.getTotalWorkingDays())
                .absentDays(internship.getAbsentDays())
                .isMultidisciplinary(internship.isMultidisciplinary())
                .absenceCompliant(internship.isAbsenceCompliant())
                .student(studentDto)
                .company(companyDto)
                .academicPeriod(academicPeriodDto)
                .report(reportDto)
                .evaluation(evaluationDto)
                .build();
    }
}
