package com.hacettepe.iwms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryDto {
    private Long id;
    private String studentNumber;
    private String name;
    private String surname;
    private String department;
    private String currentYear;
    private String email;
}
