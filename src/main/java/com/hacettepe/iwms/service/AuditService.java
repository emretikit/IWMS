package com.hacettepe.iwms.service;

public interface AuditService {
    void log(Long userId, String action, String entityType, Long entityId, String details);
}
