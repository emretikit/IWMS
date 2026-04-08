package com.hacettepe.iwms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RuleConfigRequest {
    @NotBlank
    private String ruleKey;
    @NotBlank
    private String ruleValue;
    private String description;
}
