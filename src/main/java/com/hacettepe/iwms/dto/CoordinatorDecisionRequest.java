package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.InternshipStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CoordinatorDecisionRequest {
    @NotNull
    private InternshipStatus status;
    @NotBlank
    private String feedback;
    private LocalDate revisionDeadline;
}
