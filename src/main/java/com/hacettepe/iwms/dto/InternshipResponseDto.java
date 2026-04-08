package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.InternshipStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InternshipResponseDto {
    private Long id;
    private String studentName;
    private String companyName;
    private InternshipStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalWorkingDays;
    private boolean hasReport; // To check if a report has been submitted
    private boolean hasEvaluation; // To check if an evaluation has been made
}
