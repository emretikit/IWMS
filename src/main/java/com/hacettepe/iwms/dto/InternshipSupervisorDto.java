package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.EngineerType;
import lombok.Data;

@Data
public class InternshipSupervisorDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private String companyEmail;
    private EngineerType engineerType;
}
