package com.hacettepe.iwms.service;

import com.hacettepe.iwms.entity.AuditLog;
import com.hacettepe.iwms.entity.User;
import com.hacettepe.iwms.repository.AuditLogRepository;
import com.hacettepe.iwms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public void log(Long userId, String action, String entityType, Long entityId, String details) {
        Optional<User> user = userId == null ? Optional.empty() : userRepository.findById(userId);
        AuditLog log = AuditLog.builder()
                .user(user.orElse(null))
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(details)
                .build();
        auditLogRepository.save(log);
    }
}
