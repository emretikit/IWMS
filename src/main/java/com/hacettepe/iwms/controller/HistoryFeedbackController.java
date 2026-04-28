package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.FeedbackSurveyRequest;
import com.hacettepe.iwms.dto.InternshipHistoryDto;
import com.hacettepe.iwms.entity.FeedbackSurvey;
import com.hacettepe.iwms.entity.Internship;
import com.hacettepe.iwms.entity.InternshipStatus;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.repository.FeedbackSurveyRepository;
import com.hacettepe.iwms.repository.InternshipRepository;
import com.hacettepe.iwms.repository.UserRepository;
import com.hacettepe.iwms.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryFeedbackController {
    private final InternshipRepository internshipRepository;
    private final FeedbackSurveyRepository feedbackSurveyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @GetMapping("/internships")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR')")
    public ResponseEntity<ApiResponse<InternshipHistoryDto>> internshipHistory(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<Internship> internships = internshipRepository.findByStudentUserId(currentUser.getId());

        // Bu mantık, stajların adında veya bir özelliğinde hangi staj olduğunu belirttiğini varsayar.
        // Gerçek bir senaryoda bu, AcademicPeriod veya başka bir alanla daha sağlam bir şekilde ilişkilendirilebilir.
        boolean bbm325Completed = internships.stream()
                .anyMatch(i -> i.getStatus() == InternshipStatus.APPROVED && i.getAcademicPeriod().getName().contains("BBM325"));
        boolean bbm425Completed = internships.stream()
                .anyMatch(i -> i.getStatus() == InternshipStatus.APPROVED && i.getAcademicPeriod().getName().contains("BBM425"));

        String message = "Compliance status checked. BBM325: " + (bbm325Completed ? "Completed" : "Pending") +
                         ", BBM425: " + (bbm425Completed ? "Completed" : "Pending");

        InternshipHistoryDto historyDto = InternshipHistoryDto.builder()
                .internships(internships)
                .bbm325Completed(bbm325Completed)
                .bbm425Completed(bbm425Completed)
                .complianceStatusMessage(message)
                .build();

        return ResponseEntity.ok(new ApiResponse<>(true, "Internship history and compliance status listed.", historyDto));
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR')")
    public ResponseEntity<ApiResponse<FeedbackSurvey>> submitFeedback(@Valid @RequestBody FeedbackSurveyRequest request,
                                                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        Internship internship = internshipRepository.findById(request.getInternshipId())
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        FeedbackSurvey survey = FeedbackSurvey.builder()
                .internship(internship)
                .submittedBy(user)
                .answersJson(request.getAnswersJson())
                .build();
        FeedbackSurvey saved = feedbackSurveyRepository.save(survey);
        auditService.log(currentUser.getId(), "FEEDBACK_SUBMIT", "FEEDBACK", saved.getId(), "submitted");
        return new ResponseEntity<>(new ApiResponse<>(true, "Feedback submitted.", saved), HttpStatus.CREATED);
    }
}
