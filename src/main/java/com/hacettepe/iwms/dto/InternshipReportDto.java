package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.SubmissionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InternshipReportDto {
    private Long id;
    private Long internshipId;
    private String fileName;
    private SubmissionStatus submissionStatus;
    private LocalDateTime submittedAt;
}
