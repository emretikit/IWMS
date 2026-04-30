package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.EngineerType;
import com.hacettepe.iwms.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponseDto {
    private Role role;
    private String username;
    private String email;
    private String name;
    private String surname;
    private String title;
    private EngineerType engineerType;
    private String currentYear;
    private String department;
    private String companyName;
    private String companyAddress;
}
