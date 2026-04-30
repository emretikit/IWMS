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
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
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

    @PostMapping("/students/import")
    public ResponseEntity<ApiResponse<BulkStudentImportResponse>> importStudentsFromExcel(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Excel file is required.");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".xlsx")) {
            throw new ValidationException("Only .xlsx files are supported.");
        }

        DataFormatter formatter = new DataFormatter();
        int created = 0;
        int totalRows = 0;
        List<String> skipped = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ValidationException("Excel header row is missing.");
            }
            Map<String, Integer> headerMap = buildHeaderMap(headerRow, formatter);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                totalRows++;
                String studentNumber = getCell(row, headerMap, formatter, "ogrenci no");
                String status = getCell(row, headerMap, formatter, "durum");
                String firstName = getCell(row, headerMap, formatter, "adi");
                String lastName = getCell(row, headerMap, formatter, "soyadi");
                String faculty = getCell(row, headerMap, formatter, "fakulte");
                String department = getCell(row, headerMap, formatter, "program");
                String year = getCell(row, headerMap, formatter, "sinif");
                String gradeNote = getCell(row, headerMap, formatter, "not");
                String advisorName = getCell(row, headerMap, formatter, "danisman");
                String email = getCell(row, headerMap, formatter, "e posta");
                String agnoText = getCell(row, headerMap, formatter, "agno");
                String educationType = getCell(row, headerMap, formatter, "a tipi");
                String registrationDate = getCell(row, headerMap, formatter, "kayit tarihi");

                String username = toUsernameFromEmail(email);
                String password = "Stu@" + studentNumber;
                boolean active = !StringUtils.hasText(status) || status.toLowerCase(Locale.ROOT).contains("aktif");

                if (!StringUtils.hasText(studentNumber) || !StringUtils.hasText(email) || !StringUtils.hasText(year)) {
                    skipped.add("Row " + (i + 1) + ": missing required columns.");
                    continue;
                }
                if (!StringUtils.hasText(username)) {
                    skipped.add("Row " + (i + 1) + ": email is invalid for username generation.");
                    continue;
                }
                if (userRepository.findByUsername(username).isPresent()) {
                    skipped.add("Row " + (i + 1) + ": username already exists (" + username + ").");
                    continue;
                }
                if (userRepository.findByEmail(email).isPresent()) {
                    skipped.add("Row " + (i + 1) + ": email already exists (" + email + ").");
                    continue;
                }
                if (studentRepository.findByStudentNumber(studentNumber).isPresent()) {
                    skipped.add("Row " + (i + 1) + ": student number already exists (" + studentNumber + ").");
                    continue;
                }
                if ("1".equals(year)) {
                    skipped.add("Row " + (i + 1) + ": first-year students cannot be created for internship workflow.");
                    continue;
                }

                User user = User.builder()
                        .username(username)
                        .email(email)
                        .passwordHash(passwordEncoder.encode(password))
                        .role(Role.STUDENT)
                        .name(firstName)
                        .surname(lastName)
                        .isActive(active)
                        .build();
                User savedUser = userRepository.save(user);

                Student student = Student.builder()
                        .user(savedUser)
                        .studentNumber(studentNumber)
                        .currentYear(year)
                        .department(department)
                        .faculty(faculty)
                        .advisorName(advisorName)
                        .educationType(educationType)
                        .registrationDate(registrationDate)
                        .gradeNote(gradeNote)
                        .agno(parseAgno(agnoText))
                        .build();
                studentRepository.save(student);
                created++;
            }
        } catch (Exception ex) {
            throw new ValidationException("Failed to parse Excel file: " + ex.getMessage());
        }

        BulkStudentImportResponse response = BulkStudentImportResponse.builder()
                .totalRows(totalRows)
                .createdCount(created)
                .skipped(skipped)
                .build();
        auditService.log(currentUser.getId(), "STUDENT_BULK_IMPORT", "STUDENT", null, "created=" + created + ", totalRows=" + totalRows);
        return ResponseEntity.ok(new ApiResponse<>(true, "Student import completed.", response));
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String header = formatter.formatCellValue(headerRow.getCell(i)).trim();
            if (StringUtils.hasText(header)) {
                map.put(normalizeHeader(header), i);
            }
        }
        return map;
    }

    private String getCell(Row row, Map<String, Integer> headerMap, DataFormatter formatter, String headerBase) {
        Integer idx = headerMap.get(normalizeHeader(headerBase));
        if (idx == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(idx)).trim();
    }

    private String normalizeHeader(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replace('ö', 'o').replace('ğ', 'g').replace('ı', 'i')
                .replace('ş', 's').replace('ç', 'c').replace('ü', 'u');
        normalized = normalized.replace('-', ' ');
        normalized = normalized.replaceAll("_\\d+$", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private String toUsernameFromEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "";
        }
        String raw = email.substring(0, email.indexOf('@')).toLowerCase(Locale.ROOT);
        return raw.replaceAll("[^a-z0-9._-]", "");
    }

    private Double parseAgno(String agnoText) {
        if (!StringUtils.hasText(agnoText)) {
            return null;
        }
        try {
            return Double.valueOf(agnoText.replace(",", "."));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
