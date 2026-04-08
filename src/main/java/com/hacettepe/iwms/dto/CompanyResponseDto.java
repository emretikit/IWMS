package com.hacettepe.iwms.dto;

import com.hacettepe.iwms.entity.ApprovalStatus;
import lombok.Data;

import java.util.List;

@Data
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String address;
    private ApprovalStatus approvalStatus;
    private List<InternshipSupervisorDto> supervisors;
}
