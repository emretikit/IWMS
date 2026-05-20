package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.config.CustomUserDetails;
import com.hacettepe.iwms.dto.*;
import com.hacettepe.iwms.entity.*;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.exception.ValidationException;
import com.hacettepe.iwms.repository.*;
import com.hacettepe.iwms.service.AuditService;
import com.hacettepe.iwms.service.ICompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOperationsController {
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AnnouncementRepository announcementRepository;
    private final FaqEntryRepository faqEntryRepository;
    private final StudentRepository studentRepository;
    private final InternshipRepository internshipRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final ICompanyService companyService;

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

    @DeleteMapping("/companies/{companyId}")
    public ResponseEntity<ApiResponse<String>> deleteCompany(@PathVariable Long companyId,
                                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        User admin = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        companyService.deleteCompany(companyId, admin);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company deleted successfully.", "Company " + companyId + " has been deleted."));
    }

    @PostMapping(value = "/students/import", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<BulkStudentImportResponse>> importStudents(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Excel file is required.");
        }

        BulkStudentImportResponse result = BulkStudentImportResponse.builder()
                .totalRows(0)
                .createdCount(0)
                .skipped(new ArrayList<>())
                .build();

        Map<String, String> wantedHeaders = new HashMap<>();
        wantedHeaders.put("studentNumber", "öğrenci no");
        wantedHeaders.put("name", "adı");
        wantedHeaders.put("surname", "soyadı");
        wantedHeaders.put("department", "program");
        wantedHeaders.put("currentYear", "sınıf");
        wantedHeaders.put("email", "e-posta");

        try (InputStream input = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new ValidationException("The uploaded spreadsheet is empty.");
            }

            Map<String, Integer> columnIndex = new HashMap<>();
            short lastHeader = headerRow.getLastCellNum();
            for (int c = 0; c < lastHeader; c++) {
                String raw = formatter.formatCellValue(headerRow.getCell(c)).trim();
                if (raw.isEmpty()) continue;
                String normalized = raw.toLowerCase(Locale.ROOT).split("_", 2)[0].trim();
                for (Map.Entry<String, String> entry : wantedHeaders.entrySet()) {
                    if (normalized.equals(entry.getValue()) && !columnIndex.containsKey(entry.getKey())) {
                        columnIndex.put(entry.getKey(), c);
                    }
                }
            }

            for (String key : wantedHeaders.keySet()) {
                if (!columnIndex.containsKey(key)) {
                    throw new ValidationException("Required column missing in spreadsheet header: " + wantedHeaders.get(key));
                }
            }

            String defaultPasswordHash = passwordEncoder.encode("123456");
            List<String> skipped = result.getSkipped();
            int considered = 0;

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String studentNumber = readCell(row, columnIndex.get("studentNumber"), formatter);
                String name = readCell(row, columnIndex.get("name"), formatter);
                String surname = readCell(row, columnIndex.get("surname"), formatter);
                String department = readCell(row, columnIndex.get("department"), formatter);
                String currentYear = readCell(row, columnIndex.get("currentYear"), formatter);
                String email = readCell(row, columnIndex.get("email"), formatter);

                if (studentNumber.isEmpty() && name.isEmpty() && surname.isEmpty()
                        && department.isEmpty() && currentYear.isEmpty() && email.isEmpty()) {
                    continue;
                }

                considered++;

                if (studentNumber.isEmpty() || name.isEmpty() || surname.isEmpty()
                        || department.isEmpty() || currentYear.isEmpty() || email.isEmpty()) {
                    String label = StringUtils.hasText(studentNumber) ? studentNumber : "Row " + (r + 1);
                    skipped.add("Student " + label + " could not be created (missing required field).");
                    continue;
                }

                if (studentRepository.findByStudentNumber(studentNumber).isPresent()
                        || userRepository.findByUsername(studentNumber).isPresent()) {
                    skipped.add("Student " + studentNumber + " could not be created (already exists).");
                    continue;
                }

                if (userRepository.findByEmail(email).isPresent()) {
                    skipped.add("Student " + studentNumber + " could not be created (email already in use).");
                    continue;
                }

                try {
                    User user = User.builder()
                            .username(studentNumber)
                            .email(email)
                            .passwordHash(defaultPasswordHash)
                            .role(Role.STUDENT)
                            .name(name)
                            .surname(surname)
                            .isActive(true)
                            .build();
                    User savedUser = userRepository.save(user);

                    Student student = Student.builder()
                            .user(savedUser)
                            .studentNumber(studentNumber)
                            .department(department)
                            .currentYear(currentYear)
                            .build();
                    studentRepository.save(student);

                    result.setCreatedCount(result.getCreatedCount() + 1);
                } catch (Exception ex) {
                    skipped.add("Student " + studentNumber + " could not be created.");
                }
            }

            result.setTotalRows(considered);
            auditService.log(currentUser.getId(), "STUDENT_BULK_IMPORT", "STUDENT", null,
                    "created=" + result.getCreatedCount() + " skipped=" + skipped.size());
        } catch (java.io.IOException ex) {
            throw new ValidationException("Could not read Excel file: " + ex.getMessage());
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Bulk import completed.", result));
    }

    private static String readCell(Row row, Integer index, DataFormatter formatter) {
        if (index == null) return "";
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<Page<StudentSummaryDto>>> listStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "studentNumber"));
        Page<StudentSummaryDto> result = studentRepository.findAll(pageable).map(student -> StudentSummaryDto.builder()
                .id(student.getId())
                .studentNumber(student.getStudentNumber())
                .name(student.getUser() != null ? student.getUser().getName() : null)
                .surname(student.getUser() != null ? student.getUser().getSurname() : null)
                .department(student.getDepartment())
                .currentYear(student.getCurrentYear())
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .build());
        return ResponseEntity.ok(new ApiResponse<>(true, "Students listed.", result));
    }

    @DeleteMapping("/students/{studentId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteStudent(@PathVariable Long studentId,
                                                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        long internshipCount = internshipRepository.countByStudentId(studentId);
        long enrollmentCount = courseEnrollmentRepository.countByStudentId(studentId);
        if (internshipCount > 0 || enrollmentCount > 0) {
            String label = student.getStudentNumber() != null ? student.getStudentNumber() : ("#" + studentId);
            StringBuilder reason = new StringBuilder();
            if (internshipCount > 0) {
                reason.append(internshipCount).append(" internship record(s)");
            }
            if (enrollmentCount > 0) {
                if (reason.length() > 0) reason.append(" and ");
                reason.append(enrollmentCount).append(" course enrollment(s)");
            }
            throw new ValidationException("Student " + label + " could not be deleted because they have " + reason + " linked to their account.");
        }

        User linkedUser = student.getUser();
        String label = student.getStudentNumber() != null ? student.getStudentNumber() : ("#" + studentId);

        studentRepository.delete(student);

        if (linkedUser != null) {
            // Preserve audit history by detaching the user reference instead of deleting log rows.
            List<AuditLog> userAuditLogs = auditLogRepository.findByUserId(linkedUser.getId());
            for (AuditLog logEntry : userAuditLogs) {
                logEntry.setUser(null);
            }
            if (!userAuditLogs.isEmpty()) {
                auditLogRepository.saveAll(userAuditLogs);
            }

            // Notifications and password reset tokens have non-null FKs; remove them outright.
            notificationRepository.deleteAll(notificationRepository.findByUserId(linkedUser.getId()));
            passwordResetTokenRepository.deleteAll(passwordResetTokenRepository.findByUserId(linkedUser.getId()));

            userRepository.delete(linkedUser);
        }

        auditService.log(currentUser.getId(), "STUDENT_DELETE", "STUDENT", studentId, label);
        return ResponseEntity.ok(new ApiResponse<>(true, "Student deleted successfully.", "Student " + label + " has been deleted."));
    }
}
