package com.hacettepe.iwms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackSurveyRequest {
    @NotNull
    private Long internshipId;
    @NotBlank
    private String answersJson;
}
