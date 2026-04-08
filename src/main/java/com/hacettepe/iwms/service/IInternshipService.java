package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.InternshipApplicationRequest;
import com.hacettepe.iwms.dto.InternshipResponseDto;
import com.hacettepe.iwms.dto.InternshipReportDto;
import com.hacettepe.iwms.entity.InternshipReport;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IInternshipService {
    InternshipResponseDto applyForInternship(InternshipApplicationRequest req, Long studentUserId);
    InternshipResponseDto approveByToken(String token, String verificationCode);
    InternshipResponseDto rejectByToken(String token, String verificationCode);
    List<InternshipResponseDto> getStudentInternships(Long studentUserId);
    InternshipResponseDto getInternshipById(Long internshipId);
    InternshipResponseDto getInternshipByToken(String token);
    InternshipReportDto submitReport(Long internshipId, MultipartFile file, Long studentUserId);
    InternshipReport saveReportDraft(Long internshipId, Long studentUserId, String templateContent);
}
