package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.EngineerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRegistrationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    private EngineerType engineerType;

    private String supervisorFirstName;

    private String supervisorLastName;

    private String supervisorTitle;

    @NotBlank
    @Email
    private String supervisorEmail;
}
