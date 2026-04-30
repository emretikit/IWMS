package com.hacettepe.iwms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupervisorCompanyProfileUpdateRequest {
    @NotBlank
    private String companyName;

    @NotBlank
    private String companyAddress;
}
