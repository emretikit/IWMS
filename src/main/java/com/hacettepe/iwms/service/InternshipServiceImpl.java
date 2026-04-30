package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.InternshipApplicationRequest;
import com.hacettepe.iwms.dto.InternshipReportDto;
import com.hacettepe.iwms.dto.InternshipReportSubmitRequest;
import com.hacettepe.iwms.dto.InternshipResponseDto;
import com.hacettepe.iwms.entity.*;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.mapper.InternshipMapper;
import com.hacettepe.iwms.mapper.InternshipReportMapper;
import com.hacettepe.iwms.repository.*;
import com.hacettepe.iwms.service.validators.InternshipRuleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternshipServiceImpl implements IInternshipService {

    private final InternshipRepository internshipRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final InternshipSupervisorRepository supervisorRepository;
    private final UserRepository userRepository;
    private final InternshipReportRepository internshipReportRepository;
    private final InternshipMapper internshipMapper;
    private final InternshipReportMapper internshipReportMapper;
    private final FileStorageService fileStorageService;
    private final CompanyEvaluationRepository companyEvaluationRepository;
    private final List<InternshipRuleValidator> ruleValidators;

    @Override
    @Transactional
    public InternshipResponseDto applyForInternship(InternshipApplicationRequest req, Long studentUserId) {
        try {
            Student student = studentRepository.findByUserId(studentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found for user ID: " + studentUserId));
            Company company = companyRepository.findById(req.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + req.getCompanyId()));
            if (company.getApprovalStatus() != ApprovalStatus.APPROVED) {
                throw new ValidationException("Selected company is not approved.");
            }
            AcademicPeriod period = academicPeriodRepository.findById(req.getAcademicPeriodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic Period not found with ID: " + req.getAcademicPeriodId()));
            InternshipSupervisor supervisor;
            if (req.getSupervisorId() == null) {
                supervisor = supervisorRepository.findByCompanyId(company.getId()).stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No supervisor registered for selected company."));
            } else {
                supervisor = supervisorRepository.findById(req.getSupervisorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found with ID: " + req.getSupervisorId()));
            }

            Internship internship = new Internship();
            internship.setStudent(student);
            internship.setCompany(company);
            internship.setAcademicPeriod(period);
            internship.setSupervisor(supervisor);
            internship.setStartDate(req.getStartDate());
            internship.setEndDate(req.getEndDate());
            internship.setTotalWorkingDays(req.getTotalWorkingDays());
            internship.setStatus(InternshipStatus.PENDING_COMPANY_APPROVAL);

            if (req.getStartDate().isAfter(req.getEndDate())) {
                throw new ValidationException("Start date cannot be after end date.");
            }

            if (req.getTotalWorkingDays() == null || req.getTotalWorkingDays() < period.getMinInternshipDays()) {
                throw new ValidationException("Total working days has to be at least " + period.getMinInternshipDays() + ".");
            }

            for (InternshipRuleValidator validator : ruleValidators) {
                validator.validate(internship, student, period);
            }

            Internship savedInternship = internshipRepository.save(internship);

            log.info("Internship {} created and waiting supervisor approval.", savedInternship.getId());

            return internshipMapper.toDto(savedInternship);
        } catch (ResourceNotFoundException | ValidationException e) {
            log.error("Error during internship application: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred during internship application: {}", e.getMessage(), e);
            throw new ValidationException("Application could not be completed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public InternshipReportDto submitReport(Long internshipId, InternshipReportSubmitRequest request, MultipartFile file, Long studentUserId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with ID: " + internshipId));

        if (!internship.getStudent().getUser().getId().equals(studentUserId)) {
            throw new ValidationException("You are not authorized to submit a report for this internship.");
        }

        if (internship.getStatus() != InternshipStatus.COMPLETED && internship.getStatus() != InternshipStatus.REVISION_REQUIRED) {
            throw new ValidationException("You can only submit a report for a completed internship.");
        }

        AcademicPeriod period = internship.getAcademicPeriod();
        if (period.getSubmissionDeadline() != null && LocalDate.now().isAfter(period.getSubmissionDeadline())) {
            throw new ValidationException("The submission deadline has passed.");
        }

        validateReportRequest(request, file);

        String fileName = fileStorageService.store(file, "reports/internship-" + internshipId);

        InternshipReport report = internshipReportRepository.findByInternshipId(internshipId).orElse(new InternshipReport());
        report.setInternship(internship);
        report.setFilePath(fileName);
        report.setFileName(file.getOriginalFilename());
        report.setTemplateContent(buildTemplateContent(request));
        report.setSubmittedAt(LocalDateTime.now());
        report.setDraft(false);
        report.setSubmissionStatus(SubmissionStatus.SUBMITTED);

        InternshipReport savedReport = internshipReportRepository.save(report);

        internship.setStatus(InternshipStatus.PENDING_COORDINATOR_REVIEW);
        internshipRepository.save(internship);

        return internshipReportMapper.toDto(savedReport);
    }

    @Override
    @Transactional
    public InternshipReport saveReportDraft(Long internshipId, Long studentUserId, String templateContent) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with ID: " + internshipId));
        if (!internship.getStudent().getUser().getId().equals(studentUserId)) {
            throw new ValidationException("You are not authorized to save a draft for this internship.");
        }
        InternshipReport report = internshipReportRepository.findByInternshipId(internshipId).orElse(new InternshipReport());
        report.setInternship(internship);
        report.setTemplateContent(templateContent);
        report.setDraft(true);
        report.setSubmissionStatus(SubmissionStatus.DRAFT);
        return internshipReportRepository.save(report);
    }

    @Override
    @Transactional
    public InternshipResponseDto approveBySupervisor(Long internshipId, Long supervisorUserId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with ID: " + internshipId));
        validateSupervisorAccess(internship, supervisorUserId);
        internship.setStatus(InternshipStatus.APPROVED);
        internshipRepository.save(internship);
        return internshipMapper.toDto(internship);
    }

    @Override
    @Transactional
    public InternshipResponseDto rejectBySupervisor(Long internshipId, Long supervisorUserId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with ID: " + internshipId));
        validateSupervisorAccess(internship, supervisorUserId);
        internship.setStatus(InternshipStatus.REJECTED);
        internshipRepository.save(internship);
        return internshipMapper.toDto(internship);
    }

    @Override
    @Transactional
    public InternshipResponseDto completeBySupervisor(Long internshipId,
                                                      Long supervisorUserId,
                                                      MultipartFile internshipResultDocument,
                                                      MultipartFile reportEvaluationDocument,
                                                      MultipartFile signatureFile) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with ID: " + internshipId));
        validateSupervisorOwnership(internship, supervisorUserId);
        if (internship.getStatus() != InternshipStatus.APPROVED) {
            throw new ValidationException("Only approved internships can be marked as completed.");
        }

        validatePdfFile(internshipResultDocument, "Internship result document");
        validatePdfFile(reportEvaluationDocument, "Report evaluation document");
        validateFile(signatureFile, "Signature file");

        String storageFolder = "evaluations/internship-" + internshipId;
        String internshipResultDocumentPath = fileStorageService.store(internshipResultDocument, storageFolder);
        String reportEvaluationDocumentPath = fileStorageService.store(reportEvaluationDocument, storageFolder);
        String signatureFilePath = fileStorageService.store(signatureFile, storageFolder);

        CompanyEvaluation evaluation = companyEvaluationRepository.findByInternshipId(internshipId).orElse(new CompanyEvaluation());
        evaluation.setInternship(internship);
        evaluation.setInternshipResultDocument(internshipResultDocumentPath);
        evaluation.setReportEvaluationDocument(reportEvaluationDocumentPath);
        evaluation.setSignatureFilePath(signatureFilePath);
        evaluation.setSignatureSha256(calculateSha256(signatureFile));
        evaluation.setSubmittedAt(LocalDateTime.now());
        companyEvaluationRepository.save(evaluation);

        internship.setStatus(InternshipStatus.COMPLETED);
        internshipRepository.save(internship);
        return internshipMapper.toDto(internship);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipResponseDto> getStudentInternships(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for user ID: " + studentUserId));
        return internshipMapper.toDtoList(internshipRepository.findByStudentId(student.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipResponseDto> getSupervisorInternships(Long supervisorUserId) {
        User supervisorUser = userRepository.findById(supervisorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor user not found."));
        if (supervisorUser.getRole() != Role.SUPERVISOR) {
            throw new ValidationException("Only supervisors can access this list.");
        }
        if (!StringUtils.hasText(supervisorUser.getEmail())) {
            throw new ValidationException("Supervisor email could not be validated.");
        }

        List<Internship> internships = internshipRepository.findBySupervisorCompanyEmailIgnoreCase(supervisorUser.getEmail());
        return internshipMapper.toDtoList(internships);
    }

    @Override
    @Transactional(readOnly = true)
    public InternshipResponseDto getInternshipById(Long internshipId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with ID: " + internshipId));
        return internshipMapper.toDto(internship);
    }

    private void validateSupervisorOwnership(Internship internship, Long supervisorUserId) {
        InternshipSupervisor internshipSupervisor = internship.getSupervisor();
        if (internshipSupervisor == null) {
            throw new ValidationException("No supervisor assigned to this internship.");
        }
        User supervisorUser = userRepository.findById(supervisorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor user not found."));
        if (supervisorUser.getRole() != Role.SUPERVISOR) {
            throw new ValidationException("Only supervisors can approve or reject internship applications.");
        }
        if (!StringUtils.hasText(supervisorUser.getEmail()) || !StringUtils.hasText(internshipSupervisor.getCompanyEmail())) {
            throw new ValidationException("Supervisor email could not be validated.");
        }
        if (!supervisorUser.getEmail().equalsIgnoreCase(internshipSupervisor.getCompanyEmail())) {
            throw new ValidationException("You are not authorized to process this internship.");
        }
    }

    private void validateSupervisorAccess(Internship internship, Long supervisorUserId) {
        validateSupervisorOwnership(internship, supervisorUserId);
        if (internship.getStatus() != InternshipStatus.PENDING_COMPANY_APPROVAL) {
            throw new ValidationException("This internship is not waiting for supervisor decision.");
        }
    }

    private void validateReportRequest(InternshipReportSubmitRequest request, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("A PDF report file is required.");
        }

        String originalFilename = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "";
        String lowerCaseName = originalFilename.toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!lowerCaseName.endsWith(".pdf") || !contentType.contains("pdf")) {
            throw new ValidationException("Only PDF files are accepted.");
        }
    }

    private void validatePdfFile(MultipartFile file, String label) {
        validateFile(file, label);
        String originalFilename = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "";
        String lowerCaseName = originalFilename.toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!lowerCaseName.endsWith(".pdf") || !contentType.contains("pdf")) {
            throw new ValidationException(label + " must be a PDF file.");
        }
    }

    private void validateFile(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException(label + " is required.");
        }
    }

    private String calculateSha256(MultipartFile file) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(file.getBytes());
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new ValidationException("Signature hash could not be calculated.");
        }
    }

    private String buildTemplateContent(InternshipReportSubmitRequest request) {
        return """
                Report Title: %s

                Introduction:
                %s

                Company Overview:
                %s

                Work Performed:
                %s

                Technologies Used:
                %s

                Outcomes And Learning:
                %s

                Conclusion:
                %s
                """.formatted(
                request.getReportTitle(),
                request.getIntroduction(),
                request.getCompanyOverview(),
                request.getWorkPerformed(),
                request.getTechnologiesUsed(),
                request.getOutcomesAndLearning(),
                request.getConclusion()
        );
    }
}
