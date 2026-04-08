package com.hacettepe.iwms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportDraftRequest {
    @NotBlank
    private String templateContent;
}
