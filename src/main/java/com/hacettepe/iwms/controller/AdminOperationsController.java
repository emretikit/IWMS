package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.*;
import com.hacettepe.iwms.entity.*;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.*;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOperationsController {
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AnnouncementRepository announcementRepository;
    private final FaqEntryRepository faqEntryRepository;
    private final AuditService auditService;

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(@PathVariable Long userId,
                                                              @Valid @RequestBody UserStatusRequest request,
                                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN && !request.getActive()) {
            long activeAdminCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.ADMIN && u.isActive()).count();
            if (activeAdminCount <= 1) {
                throw new ValidationException("Cannot deactivate the last active admin.");
            }
        }
        user.setActive(request.getActive());
        User saved = userRepository.save(user);
        auditService.log(currentUser.getId(), "USER_STATUS_UPDATE", "USER", userId, request.getActive().toString());
        return ResponseEntity.ok(new ApiResponse<>(true, "User status updated.", saved));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(@RequestParam(required = false) String from,
                                                                    @RequestParam(required = false) String to) {
        if (from == null || to == null) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Audit logs listed.", auditLogRepository.findAll()));
        }
        LocalDateTime start = LocalDateTime.parse(from);
        LocalDateTime end = LocalDateTime.parse(to);
        return ResponseEntity.ok(new ApiResponse<>(true, "Audit logs listed.", auditLogRepository.findByCreatedAtBetween(start, end)));
    }

    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<Announcement>> createAnnouncement(@Valid @RequestBody AnnouncementRequest request,
                                                                        @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .createdBy(user)
                .build();
        Announcement saved = announcementRepository.save(announcement);
        auditService.log(currentUser.getId(), "ANNOUNCEMENT_CREATE", "ANNOUNCEMENT", saved.getId(), saved.getTitle());
        return new ResponseEntity<>(new ApiResponse<>(true, "Announcement created.", saved), HttpStatus.CREATED);
    }

    @PostMapping("/faqs")
    public ResponseEntity<ApiResponse<FaqEntry>> createFaq(@Valid @RequestBody FaqRequest request,
                                                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        FaqEntry faq = FaqEntry.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .category(request.getCategory())
                .build();
        FaqEntry saved = faqEntryRepository.save(faq);
        auditService.log(currentUser.getId(), "FAQ_CREATE", "FAQ", saved.getId(), saved.getQuestion());
        return new ResponseEntity<>(new ApiResponse<>(true, "FAQ created.", saved), HttpStatus.CREATED);
    }
}
