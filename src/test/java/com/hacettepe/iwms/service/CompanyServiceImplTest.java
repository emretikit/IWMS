package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.CompanyRegistrationRequest;
import com.hacettepe.iwms.entity.*;
import com.hacettepe.iwms.mapper.CompanyMapper;
import com.hacettepe.iwms.repository.CompanyRepository;
import com.hacettepe.iwms.repository.InternshipSupervisorRepository;
import com.hacettepe.iwms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {
    @Mock private CompanyRepository companyRepository;
    @Mock private InternshipSupervisorRepository supervisorRepository;
    @Mock private UserRepository userRepository;
    @Mock private CompanyMapper companyMapper;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;
    @InjectMocks private CompanyServiceImpl companyService;

    @Test
    void registerCompany_shouldSetPendingStatus() {
        CompanyRegistrationRequest req = new CompanyRegistrationRequest();
        req.setName("ACME");
        req.setAddress("Ankara");
        req.setEngineerType(EngineerType.COMPUTER);
        req.setSupervisorFirstName("A");
        req.setSupervisorLastName("B");
        req.setSupervisorTitle("Eng");
        req.setSupervisorEmail("a@b.com");
        Company saved = Company.builder().id(1L).name("ACME").approvalStatus(ApprovalStatus.PENDING).build();
        when(companyRepository.save(any())).thenReturn(saved);
        when(companyMapper.toCompanyResponseDto(any())).thenReturn(new com.hacettepe.iwms.dto.CompanyResponseDto());

        com.hacettepe.iwms.dto.CompanyResponseDto response = companyService.registerCompany(req);
        assertEquals(null, response.getId());
    }
}
