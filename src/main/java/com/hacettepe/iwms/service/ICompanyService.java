package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.CompanyRegistrationRequest;
import com.hacettepe.iwms.dto.CompanyResponseDto;
import com.hacettepe.iwms.entity.User;

import java.util.List;

public interface ICompanyService {
    CompanyResponseDto registerCompany(CompanyRegistrationRequest req);
    CompanyResponseDto approveCompany(Long companyId, User admin);
    CompanyResponseDto rejectCompany(Long companyId, User admin, String reason);
    List<CompanyResponseDto> getPendingCompanies();
    List<CompanyResponseDto> getAllCompanies();
    List<CompanyResponseDto> getApprovedCompanies();
    CompanyResponseDto getCompanyById(Long companyId);
    void deleteCompany(Long companyId, User admin);
}
