package com.example.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private Long entityId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditAction action;
    
    @Column(nullable = false, length = 2000)
    private String details;
    
    private Long userId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public AuditLog() {
        this.createdAt = LocalDateTime.now();
    }
    
    public AuditLog(String entityType, Long entityId, AuditAction action, String details, Long userId) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.details = details;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Enum for Audit Actions
    public enum AuditAction {
        ORDER_CREATED,
        ORDER_VALIDATED,
        ORDER_PAYMENT_INITIATED,
        ORDER_PAYMENT_COMPLETED,
        ORDER_PAYMENT_FAILED,
        WALLET_DEBITED,
        WALLET_CREDITED,
        WALLET_FEE_COLLECTED,
        MERCHANT_CREDITED,
        NOTIFICATION_SENT,
        NOTIFICATION_FAILED,
        TRANSACTION_ROLLED_BACK
    }
    
    // Getters and Setters
    public Long getAuditId() {
        return auditId;
    }
    
    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public AuditAction getAction() {
        return action;
    }
    
    public void setAction(AuditAction action) {
        this.action = action;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
