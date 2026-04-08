package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.FeedbackSurveyRequest;
import com.hacettepe.iwms.entity.FeedbackSurvey;
import com.hacettepe.iwms.entity.Internship;
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
    public ResponseEntity<ApiResponse<List<Internship>>> internshipHistory(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Internship history listed.", internshipRepository.findByStudentUserId(currentUser.getId())));
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
