package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.EngineerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyRegistrationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotNull
    private EngineerType engineerType;

    @NotBlank
    private String supervisorFirstName;

    @NotBlank
    private String supervisorLastName;

    @NotBlank
    private String supervisorTitle;

    @NotBlank
    @Email
    private String supervisorEmail;
}
