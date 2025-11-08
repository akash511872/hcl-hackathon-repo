package com.example.demo.service;

import com.example.demo.model.AuditLog;
import com.example.demo.model.Notification;
import com.example.demo.model.Order;
import com.example.demo.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Send notification to merchant
     * Retry once if failed, otherwise log to transaction ledger
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void sendMerchantNotification(Order order, String merchantEmail, String message) {
        Notification notification = new Notification();
        notification.setRecipient(merchantEmail);
        notification.setNotificationType(Notification.NotificationType.EMAIL);
        notification.setMessage(message);
        notification.setOrder(order);
        
        try {
            // Simulate sending notification (in real scenario, this would call email service)
            logger.info("Sending notification to merchant: {} - {}", merchantEmail, message);
            notification.setStatus(Notification.NotificationStatus.SENT);
            notificationRepository.save(notification);
            
            auditService.logAudit("Notification", notification.getNotificationId(), 
                AuditLog.AuditAction.NOTIFICATION_SENT, 
                "Merchant notification sent successfully to " + merchantEmail);
        } catch (Exception e) {
            logger.error("Failed to send merchant notification: {}", e.getMessage());
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notification.setRetryCount(1);
            
            // Retry once
            try {
                logger.info("Retrying merchant notification: {}", merchantEmail);
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setFailureReason(null);
                notificationRepository.save(notification);
                
                auditService.logAudit("Notification", notification.getNotificationId(), 
                    AuditLog.AuditAction.NOTIFICATION_SENT, 
                    "Merchant notification sent on retry to " + merchantEmail);
            } catch (Exception retryException) {
                logger.error("Retry failed for merchant notification: {}", retryException.getMessage());
                notification.setStatus(Notification.NotificationStatus.LOGGED);
                notification.setFailureReason(retryException.getMessage());
                notificationRepository.save(notification);
                
                auditService.logAudit("Notification", notification.getNotificationId(), 
                    AuditLog.AuditAction.NOTIFICATION_FAILED, 
                    "Failed to send merchant notification, logged to ledger: " + retryException.getMessage());
            }
        }
    }
    
    /**
     * Send notification to customer
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void sendCustomerNotification(Order order, String customerEmail, String message) {
        Notification notification = new Notification();
        notification.setRecipient(customerEmail);
        notification.setNotificationType(Notification.NotificationType.EMAIL);
        notification.setMessage(message);
        notification.setOrder(order);
        
        try {
            // Simulate sending notification
            logger.info("Sending notification to customer: {} - {}", customerEmail, message);
            notification.setStatus(Notification.NotificationStatus.SENT);
            notificationRepository.save(notification);
            
            auditService.logAudit("Notification", notification.getNotificationId(), 
                AuditLog.AuditAction.NOTIFICATION_SENT, 
                "Customer notification sent successfully to " + customerEmail);
        } catch (Exception e) {
            logger.error("Failed to send customer notification: {}", e.getMessage());
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notificationRepository.save(notification);
            
            auditService.logAudit("Notification", notification.getNotificationId(), 
                AuditLog.AuditAction.NOTIFICATION_FAILED, 
                "Failed to send customer notification: " + e.getMessage());
        }
    }
}
