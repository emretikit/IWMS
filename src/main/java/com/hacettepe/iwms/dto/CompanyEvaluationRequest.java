package com.hacettepe.iwms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyEvaluationRequest {
    @NotBlank
    private String internshipResultDocument;
    @NotBlank
    private String reportEvaluationDocument;
    @NotBlank
    private String signatureFilePath;
}
