package com.hacettepe.iwms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hacettepe.iwms.entity.InternshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatorPendingInternshipDto {
    private Long id;
    private InternshipStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalWorkingDays;
    private int absentDays;
    @JsonProperty("isMultidisciplinary")
    private boolean isMultidisciplinary;
    private boolean absenceCompliant;
    private StudentDto student;
    private CompanyDto company;
    private AcademicPeriodDto academicPeriod;
    private ReportDto report;
    private EvaluationDto evaluation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentDto {
        private Long id;
        private String studentNumber;
        private String department;
        private String currentYear;
        private UserDto user;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private String name;
        private String surname;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyDto {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicPeriodDto {
        private Long id;
        private String name;
        private LocalDate submissionDeadline;
        private Integer minInternshipDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportDto {
        private Long id;
        private String submissionStatus;
        private boolean draft;
        private LocalDateTime submittedAt;
        private String fileName;
        private String templateContent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationDto {
        private Long id;
        private String result;
        private String feedback;
        private boolean documentsComplete;
        private boolean rulesCompliant;
    }
}
