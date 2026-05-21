package com.hacettepe.iwms.service;

import com.hacettepe.iwms.dto.InternshipApplicationRequest;
import com.hacettepe.iwms.dto.InternshipReportSubmitRequest;
import com.hacettepe.iwms.dto.InternshipResponseDto;
import com.hacettepe.iwms.dto.InternshipReportDto;
import com.hacettepe.iwms.entity.InternshipReport;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IInternshipService {
    InternshipResponseDto applyForInternship(InternshipApplicationRequest req, Long studentUserId);
    InternshipResponseDto approveBySupervisor(Long internshipId, Long supervisorUserId);
    InternshipResponseDto rejectBySupervisor(Long internshipId, Long supervisorUserId);
    InternshipResponseDto completeBySupervisor(Long internshipId,
                                               Long supervisorUserId,
                                               MultipartFile internshipResultDocument,
                                               MultipartFile reportEvaluationDocument,
                                               MultipartFile signatureFile);
    List<InternshipResponseDto> getStudentInternships(Long studentUserId);
    List<InternshipResponseDto> getSupervisorInternships(Long supervisorUserId);
    InternshipResponseDto getInternshipById(Long internshipId);
    InternshipReportDto submitReport(Long internshipId, InternshipReportSubmitRequest request, MultipartFile file, Long studentUserId);
    InternshipReport saveReportDraft(Long internshipId, Long studentUserId, String templateContent);
}
