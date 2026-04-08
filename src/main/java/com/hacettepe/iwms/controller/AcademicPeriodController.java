package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.AcademicPeriodUpsertRequest;
import com.hacettepe.iwms.dto.RuleConfigRequest;
import com.hacettepe.iwms.entity.AcademicPeriod;
import com.hacettepe.iwms.entity.RuleConfig;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ApiResponse<AcademicPeriod>> createPeriod(@Valid @RequestBody AcademicPeriodUpsertRequest request,
                                                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateDates(request);
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
        if (request.getLateDeadline().isBefore(request.getSubmissionDeadline())) {
            throw new ValidationException("Late deadline cannot be before submission deadline.");
        }
    }
}
