package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.Internship;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InternshipHistoryDto {
    private List<Internship> internships;
    private boolean bbm325Completed;
    private boolean bbm425Completed;
    private String complianceStatusMessage;
}
