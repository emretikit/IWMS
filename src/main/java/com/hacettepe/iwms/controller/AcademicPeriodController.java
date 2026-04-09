package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.AcademicPeriodUpsertRequest;
import com.hacettepe.iwms.dto.RuleConfigRequest;
import com.hacettepe.iwms.entity.AcademicPeriod;
import com.hacettepe.iwms.entity.RuleConfig;
import com.hacettepe.iwms.entity.SemesterType;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.repository.AcademicPeriodRepository;
import com.hacettepe.iwms.repository.RuleConfigRepository;
import com.hacettepe.iwms.repository.UserRepository;
import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
public class AcademicPeriodController {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final RuleConfigRepository ruleConfigRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ApiResponse<AcademicPeriod>> createPeriod(@Valid @RequestBody AcademicPeriodUpsertRequest request,
                                                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateDates(request);
        if (request.isActive()) {
            List<AcademicPeriod> activePeriods = academicPeriodRepository.findAllByIsActiveTrueOrderByYearDesc();
            activePeriods.forEach((period) -> period.setActive(false));
            academicPeriodRepository.saveAll(activePeriods);
        }
        AcademicPeriod period = AcademicPeriod.builder()
                .name(request.getName())
                .semesterType(request.getSemesterType())
                .year(request.getYear())
                .submissionDeadline(request.getSubmissionDeadline())
                .lateDeadline(request.getLateDeadline())
                .minInternshipDays(request.getMinInternshipDays())
                .maxOrgsPerPeriod(request.getMaxOrgsPerPeriod())
                .isActive(request.isActive())
                .createdBy(user)
                .build();
        AcademicPeriod saved = academicPeriodRepository.save(period);
        auditService.log(user.getId(), "PERIOD_CREATE", "ACADEMIC_PERIOD", saved.getId(), "created");
        return new ResponseEntity<>(new ApiResponse<>(true, "Academic period created.", saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ApiResponse<AcademicPeriod>> updatePeriod(@PathVariable Long id,
                                                                    @Valid @RequestBody AcademicPeriodUpsertRequest request,
                                                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AcademicPeriod period = academicPeriodRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Period not found"));
        validateDates(request);
        period.setName(request.getName());
        period.setSemesterType(request.getSemesterType());
        period.setYear(request.getYear());
        period.setSubmissionDeadline(request.getSubmissionDeadline());
        period.setLateDeadline(request.getLateDeadline());
        period.setMinInternshipDays(request.getMinInternshipDays());
        period.setMaxOrgsPerPeriod(request.getMaxOrgsPerPeriod());
        period.setActive(request.isActive());
        AcademicPeriod saved = academicPeriodRepository.save(period);
        auditService.log(user.getId(), "PERIOD_UPDATE", "ACADEMIC_PERIOD", saved.getId(), "updated");
        return ResponseEntity.ok(new ApiResponse<>(true, "Academic period updated.", saved));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ApiResponse<List<AcademicPeriod>>> listPeriods() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Academic periods listed.", academicPeriodRepository.findAll()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','STUDENT')")
    public ResponseEntity<ApiResponse<List<AcademicPeriod>>> listActivePeriods() {
        List<AcademicPeriod> activePeriods = jdbcTemplate.query(
                """
                select id, name, semester_type, year, submission_deadline, late_deadline,
                       min_internship_days, max_orgs_per_period, is_active
                from academic_period
                where is_active = true
                order by year desc
                """,
                (rs, rowNum) -> AcademicPeriod.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .semesterType(SemesterType.valueOf(rs.getString("semester_type")))
                        .year(rs.getInt("year"))
                        .submissionDeadline(rs.getDate("submission_deadline").toLocalDate())
                        .lateDeadline(rs.getDate("late_deadline").toLocalDate())
                        .minInternshipDays(rs.getInt("min_internship_days"))
                        .maxOrgsPerPeriod(rs.getInt("max_orgs_per_period"))
                        .isActive(rs.getBoolean("is_active"))
                        .build()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Active academic periods listed.", activePeriods));
    }

    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ApiResponse<RuleConfig>> upsertRule(@Valid @RequestBody RuleConfigRequest request,
                                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        RuleConfig config = ruleConfigRepository.findByRuleKey(request.getRuleKey()).orElse(new RuleConfig());
        config.setRuleKey(request.getRuleKey());
        config.setRuleValue(request.getRuleValue());
        config.setDescription(request.getDescription());
        config.setUpdatedBy(user);
        config.setUpdatedAt(LocalDateTime.now());
        RuleConfig saved = ruleConfigRepository.save(config);
        auditService.log(user.getId(), "RULE_UPDATE", "RULE_CONFIG", saved.getId(), request.getRuleKey());
        return ResponseEntity.ok(new ApiResponse<>(true, "Rule updated.", saved));
    }

    @GetMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ApiResponse<List<RuleConfig>>> listRules() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Rules listed.", ruleConfigRepository.findAll()));
    }

    private void validateDates(AcademicPeriodUpsertRequest request) {
        if (request.getSubmissionDeadline() == null || request.getLateDeadline() == null) {
            throw new ValidationException("Submission deadline and late deadline are required.");
        }
        if (request.getLateDeadline().isBefore(request.getSubmissionDeadline())) {
            throw new ValidationException("Late deadline cannot be before submission deadline.");
        }
        if (request.getYear() == null || request.getYear() < 2000) {
            throw new ValidationException("Please provide a valid academic year.");
        }
        if (request.getMinInternshipDays() == null || request.getMinInternshipDays() < 1) {
            throw new ValidationException("Minimum internship days must be at least 1.");
        }
        if (request.getMaxOrgsPerPeriod() == null || request.getMaxOrgsPerPeriod() < 1) {
            throw new ValidationException("Maximum organizations per period must be at least 1.");
        }
    }
}
