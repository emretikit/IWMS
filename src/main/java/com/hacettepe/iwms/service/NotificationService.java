package com.hacettepe.iwms.service;

import com.hacettepe.iwms.entity.ApprovalStatus;
import com.hacettepe.iwms.entity.Company;
import com.hacettepe.iwms.entity.InternshipSupervisor;

public interface NotificationService {
    void sendCompanyStatusEmail(InternshipSupervisor supervisor, Company company, ApprovalStatus status, String reason);
}
