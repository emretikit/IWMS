package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.InternshipApplicationRequest;
import com.hacettepe.iwms.dto.InternshipReportDto;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternshipServiceImpl implements IInternshipService {

    private final InternshipRepository internshipRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final SupervisorTokenRepository supervisorTokenRepository;
    private final InternshipSupervisorRepository supervisorRepository;
    private final InternshipReportRepository internshipReportRepository;
    private final InternshipMapper internshipMapper;
    private final InternshipReportMapper internshipReportMapper;
    private final FileStorageService fileStorageService;
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

            for (InternshipRuleValidator validator : ruleValidators) {
                validator.validate(internship, student, period);
            }

            Internship savedInternship = internshipRepository.save(internship);

            String tokenValue = UUID.randomUUID().toString();
            String verificationCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            SupervisorToken supervisorToken = SupervisorToken.builder()
                    .internship(savedInternship)
                    .supervisorEmail(supervisor.getCompanyEmail())
                    .token(tokenValue)
                    .verificationCode(verificationCode)
                    .status(TokenStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();
            supervisorTokenRepository.save(supervisorToken);

            log.info("--- SUPERVISOR NOTIFICATION (SIMULATED) ---");
            log.info("To: {}", supervisor.getCompanyEmail());
            log.info("Subject: Internship Application Approval Request");
            log.info("Body: Please review the application at: http://localhost:3000/supervisor/token/{}", tokenValue);
            log.info("Your verification code is: {}", verificationCode);
            log.info("-------------------------------------------");

            return internshipMapper.toDto(savedInternship);
        } catch (ResourceNotFoundException | ValidationException e) {
            log.error("Error during internship application: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred during internship application: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during internship application.", e);
        }
    }

    @Override
    @Transactional
    public InternshipReportDto submitReport(Long internshipId, MultipartFile file, Long studentUserId) {
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

        String fileName = fileStorageService.store(file, "reports/internship-" + internshipId);

        InternshipReport report = new InternshipReport();
        report.setInternship(internship);
        report.setFilePath(fileName);
        report.setFileName(file.getOriginalFilename());
        report.setSubmittedAt(LocalDateTime.now());
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
    public InternshipResponseDto approveByToken(String token, String verificationCode) {
        SupervisorToken supervisorToken = supervisorTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found."));

        validateToken(supervisorToken, verificationCode);

        Internship internship = supervisorToken.getInternship();
        internship.setStatus(InternshipStatus.APPROVED);
        supervisorToken.setStatus(TokenStatus.VERIFIED);
        supervisorToken.setVerifiedAt(LocalDateTime.now());

        internshipRepository.save(internship);
        supervisorTokenRepository.save(supervisorToken);

        return internshipMapper.toDto(internship);
    }

    @Override
    @Transactional
    public InternshipResponseDto rejectByToken(String token, String verificationCode) {
        SupervisorToken supervisorToken = supervisorTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found."));

        validateToken(supervisorToken, verificationCode);

        Internship internship = supervisorToken.getInternship();
        internship.setStatus(InternshipStatus.REJECTED);
        supervisorToken.setStatus(TokenStatus.VERIFIED);
        supervisorToken.setVerifiedAt(LocalDateTime.now());

        internshipRepository.save(internship);
        supervisorTokenRepository.save(supervisorToken);

        return internshipMapper.toDto(internship);
    }

    @Override
    public List<InternshipResponseDto> getStudentInternships(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for user ID: " + studentUserId));
        return internshipMapper.toDtoList(internshipRepository.findByStudentId(student.getId()));
    }

    @Override
    public InternshipResponseDto getInternshipById(Long internshipId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with ID: " + internshipId));
        return internshipMapper.toDto(internship);
    }

    @Override
    public InternshipResponseDto getInternshipByToken(String token) {
        SupervisorToken supervisorToken = supervisorTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found."));
        return internshipMapper.toDto(supervisorToken.getInternship());
    }

    private void validateToken(SupervisorToken token, String verificationCode) {
        if (token.isExpired()) {
            throw new ValidationException("This link has expired.");
        }
        if (token.isVerified()) {
            throw new ValidationException("This application has already been processed.");
        }
        if (!token.canAttempt()) {
            throw new ValidationException("Maximum verification attempts exceeded.");
        }
        if (!token.getVerificationCode().equals(verificationCode)) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            supervisorTokenRepository.save(token);
            throw new ValidationException("Invalid verification code. Remaining attempts: " + (5 - token.getAttemptCount()));
        }
    }
}
