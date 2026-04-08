package com.hacettepe.iwms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, max = 40)
    private String newPassword;
}
