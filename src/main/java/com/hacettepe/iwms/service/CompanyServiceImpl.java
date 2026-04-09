package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.CompanyRegistrationRequest;
import com.hacettepe.iwms.dto.CompanyResponseDto;
import com.hacettepe.iwms.entity.*;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import com.hacettepe.iwms.mapper.CompanyMapper;
import com.hacettepe.iwms.repository.CompanyRepository;
import com.hacettepe.iwms.repository.InternshipSupervisorRepository;
import com.hacettepe.iwms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements ICompanyService {

    private final CompanyRepository companyRepository;
    private final InternshipSupervisorRepository supervisorRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CompanyResponseDto registerCompany(CompanyRegistrationRequest req) {
        // Create and save the company
        Company company = new Company();
        company.setName(req.getName());
        company.setAddress(req.getAddress());
        company.setApprovalStatus(ApprovalStatus.PENDING);
        Company savedCompany = companyRepository.save(company);

        // Create and save the internship supervisor
        InternshipSupervisor supervisor = new InternshipSupervisor();
        supervisor.setCompany(savedCompany);
        supervisor.setFirstName(req.getSupervisorFirstName());
        supervisor.setLastName(req.getSupervisorLastName());
        supervisor.setTitle(req.getSupervisorTitle());
        supervisor.setCompanyEmail(req.getSupervisorEmail());
        supervisor.setEngineerType(req.getEngineerType());
        supervisorRepository.save(supervisor);
        
        auditService.log(null, "COMPANY_REGISTER", "COMPANY", savedCompany.getId(), "New company registered and pending approval.");

        return companyMapper.toCompanyResponseDto(savedCompany);
    }

    @Override
    @Transactional
    public CompanyResponseDto approveCompany(Long companyId, User admin) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        company.setApprovalStatus(ApprovalStatus.APPROVED);
        company.setApprovedBy(admin);
        company.setApprovedAt(LocalDateTime.now());
        Company updatedCompany = companyRepository.save(company);

        auditService.log(admin.getId(), "COMPANY_APPROVE", "COMPANY", updatedCompany.getId(), "Company approved.");
        
        // Send notification to the supervisor who registered the company
        if (company.getSupervisors() != null && !company.getSupervisors().isEmpty()) {
            InternshipSupervisor supervisor = company.getSupervisors().get(0);
            notificationService.sendCompanyStatusEmail(supervisor, updatedCompany, ApprovalStatus.APPROVED, null);
        }

        return companyMapper.toCompanyResponseDto(updatedCompany);
    }

    @Override
    @Transactional
    public CompanyResponseDto rejectCompany(Long companyId, User admin, String reason) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        company.setApprovalStatus(ApprovalStatus.REJECTED);
        Company updatedCompany = companyRepository.save(company);

        auditService.log(admin.getId(), "COMPANY_REJECT", "COMPANY", updatedCompany.getId(), "Company rejected. Reason: " + reason);

        // Send notification to the supervisor who registered the company
        if (company.getSupervisors() != null && !company.getSupervisors().isEmpty()) {
            InternshipSupervisor supervisor = company.getSupervisors().get(0);
            notificationService.sendCompanyStatusEmail(supervisor, updatedCompany, ApprovalStatus.REJECTED, reason);
        }

        return companyMapper.toCompanyResponseDto(updatedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDto> getPendingCompanies() {
        return companyMapper.toCompanyResponseDtoList(companyRepository.findByApprovalStatus(ApprovalStatus.PENDING));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDto> getAllCompanies() {
        return companyMapper.toCompanyResponseDtoList(companyRepository.findAll());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDto> getApprovedCompanies() {
        return companyMapper.toCompanyResponseDtoList(companyRepository.findByApprovalStatus(ApprovalStatus.APPROVED));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDto getCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        return companyMapper.toCompanyResponseDto(company);
    }
}
