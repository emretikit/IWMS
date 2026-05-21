package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.SemesterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AcademicPeriodUpsertRequest {
    @NotBlank
    private String name;
    @NotNull
    private SemesterType semesterType;
    @NotNull
    private Integer year;
    @NotNull
    private LocalDate submissionDeadline;
    @NotNull
    private LocalDate lateDeadline;
    @NotNull
    private Integer minInternshipDays;
    @NotNull
    private Integer maxOrgsPerPeriod;
    private boolean active;
}
