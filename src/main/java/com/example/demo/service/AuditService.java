package com.example.demo.service;

import com.example.demo.model.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Log audit information
     * This method uses REQUIRES_NEW to ensure audit logs are saved even if parent transaction rolls back
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void logAudit(String entityType, Long entityId, AuditLog.AuditAction action, String details, Long userId) {
        try {
            AuditLog auditLog = new AuditLog(entityType, entityId, action, details, userId);
            auditLogRepository.save(auditLog);
            logger.info("Audit log created: {} - {} - {}", entityType, action, details);
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log audit information without user ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void logAudit(String entityType, Long entityId, AuditLog.AuditAction action, String details) {
        logAudit(entityType, entityId, action, details, null);
    }
}
