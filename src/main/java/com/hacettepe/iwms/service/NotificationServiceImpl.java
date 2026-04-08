package com.hacettepe.iwms.service;

import com.hacettepe.iwms.entity.ApprovalStatus;
import com.hacettepe.iwms.entity.Company;
import com.hacettepe.iwms.entity.InternshipSupervisor;
import com.hacettepe.iwms.entity.Notification;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.repository.NotificationRepository;
import com.hacettepe.iwms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void sendCompanyStatusEmail(InternshipSupervisor supervisor, Company company, ApprovalStatus status, String reason) {
        Optional<User> relatedUser = userRepository.findByEmail(supervisor.getCompanyEmail());
        if (relatedUser.isEmpty()) {
            return;
        }

        String title = "Company Registration " + status.name();
        String message = "Company " + company.getName() + " status updated to " + status.name();
        if (reason != null && !reason.isBlank()) {
            message += ". Reason: " + reason;
        }

        Notification notification = Notification.builder()
                .user(relatedUser.get())
                .type("COMPANY_REGISTRATION")
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }
}
