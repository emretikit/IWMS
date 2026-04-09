package com.hacettepe.iwms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InternshipReportSubmitRequest {

    @NotBlank
    private String reportTitle;

    @NotBlank
    private String introduction;

    @NotBlank
    private String companyOverview;

    @NotBlank
    private String workPerformed;

    @NotBlank
    private String technologiesUsed;

    @NotBlank
    private String outcomesAndLearning;

    @NotBlank
    private String conclusion;
}
