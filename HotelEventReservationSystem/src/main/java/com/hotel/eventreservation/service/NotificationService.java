package com.hotel.eventreservation.service;

import com.hotel.eventreservation.model.Notification;
import com.hotel.eventreservation.model.User;
import com.hotel.eventreservation.repository.NotificationRepository;
import com.hotel.eventreservation.strategy.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private Map<String, NotificationStrategy> notificationStrategies;
    
    @Value("${app.notification.email-enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.notification.sms-enabled:false}")
    private boolean smsEnabled;
    
    /**
     * Send notification using multiple strategies
     */
    public boolean sendNotification(User recipient, String messageContent, Notification.AlertType alertType) {
        Notification notification = new Notification(recipient, messageContent, alertType);
        return sendNotification(notification);
    }
    
    /**
     * Send notification from staff member
     */
    public boolean sendNotification(User recipient, User sender, String messageContent, Notification.AlertType alertType) {
        Notification notification = new Notification(recipient, sender, messageContent, alertType);
        return sendNotification(notification);
    }
    
    /**
     * Send notification using configured strategies
     */
    public boolean sendNotification(Notification notification) {
        if (notification == null) {
            logger.error("Cannot send null notification");
            return false;
        }
        
        if (notification.getRecipientUser() == null) {
            logger.error("Cannot send notification without recipient user");
            return false;
        }
        
        boolean success = false;
        
        try {
            // Always save to database (In-App notification) - Direct save to avoid strategy issues
            notificationRepository.save(notification);
            success = true;
            logger.info("In-app notification saved successfully for user: {}", notification.getRecipientUser().getUsername());
            
            // Send email if enabled
            if (emailEnabled) {
                try {
                    NotificationStrategy emailStrategy = notificationStrategies.get("emailNotificationStrategy");
                    if (emailStrategy != null) {
                        emailStrategy.sendNotification(notification);
                    }
                } catch (Exception e) {
                    logger.warn("Email notification failed: {}", e.getMessage());
                }
            }
            
            // Send SMS if enabled
            if (smsEnabled) {
                try {
                    NotificationStrategy smsStrategy = notificationStrategies.get("sMSNotificationStrategy");
                    if (smsStrategy != null) {
                        smsStrategy.sendNotification(notification);
                    }
                } catch (Exception e) {
                    logger.warn("SMS notification failed: {}", e.getMessage());
                }
            }
            
            logger.info("Notification sent to user: {} with alert type: {}", 
                       notification.getRecipientUser().getUsername(), notification.getAlertType());
        } catch (Exception e) {
            logger.error("Error sending notification to user: {}", 
                        notification.getRecipientUser().getUsername(), e);
        }
        
        return success;
    }
    
    /**
     * Get notifications for user
     */
    public List<Notification> getNotificationsForUser(Long userId) {
        try {
            logger.info("Fetching notifications for user: {}", userId);
            List<Notification> notifications = notificationRepository.findByRecipientUserUserIdOrderByCreatedAtDesc(userId);
            logger.info("Successfully fetched {} notifications for user: {}", notifications.size(), userId);
            return notifications;
        } catch (Exception e) {
            logger.error("Error fetching notifications for user {}: {}", userId, e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get unread notifications for user
     */
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        try {
            logger.info("Fetching unread notifications for user: {}", userId);
            List<Notification> notifications = notificationRepository.findByRecipientUserUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
            logger.info("Successfully fetched {} unread notifications for user: {}", notifications.size(), userId);
            return notifications;
        } catch (Exception e) {
            logger.error("Error fetching unread notifications for user {}: {}", userId, e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get read notifications for user
     */
    public List<Notification> getReadNotificationsForUser(Long userId) {
        try {
            return notificationRepository.findByRecipientUserUserIdAndIsReadTrueOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            logger.error("Error fetching read notifications for user {}: {}", userId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Mark notification as read
     */
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            throw new RuntimeException("Notification not found");
        }
        
        Notification notification = notificationOpt.get();
        notification.markAsRead();
        
        notification = notificationRepository.save(notification);
        logger.info("Notification marked as read: {}", notificationId);
        return notification;
    }
    
    /**
     * Get notification count for user
     */
    public Long getUnreadNotificationCount(Long userId) {
        try {
            logger.info("Fetching unread notification count for user: {}", userId);
            Long count = notificationRepository.countUnreadNotificationsByUserId(userId);
            logger.info("Successfully fetched unread notification count {} for user: {}", count, userId);
            return count;
        } catch (Exception e) {
            logger.error("Error fetching unread notification count for user {}: {}", userId, e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
    
    /**
     * Get notifications by alert type
     */
    public List<Notification> getNotificationsByAlertType(Notification.AlertType alertType) {
        try {
            return notificationRepository.findByAlertType(alertType);
        } catch (Exception e) {
            logger.error("Error fetching notifications by alert type {}: {}", alertType, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get notifications by user and alert type
     */
    public List<Notification> getNotificationsByUserAndAlertType(Long userId, Notification.AlertType alertType) {
        try {
            return notificationRepository.findByRecipientUserAndAlertType(userId, alertType);
        } catch (Exception e) {
            logger.error("Error fetching notifications by user and alert type for user {}: {}", userId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get notifications since specific date
     */
    public List<Notification> getNotificationsSince(LocalDateTime since) {
        try {
            return notificationRepository.findNotificationsSince(since);
        } catch (Exception e) {
            logger.error("Error fetching notifications since {}: {}", since, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get notifications by date range
     */
    public List<Notification> getNotificationsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return notificationRepository.findByRecipientUserAndDateRange(userId, startDate, endDate);
        } catch (Exception e) {
            logger.error("Error fetching notifications by date range for user {}: {}", userId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        logger.info("Notification deleted: {}", notificationId);
    }
    
    /**
     * Delete notification for specific user (security check)
     */
    public boolean deleteNotificationForUser(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (notification.getRecipientUser().getUserId().equals(userId)) {
                notificationRepository.deleteById(notificationId);
                logger.info("Notification {} deleted for user {}", notificationId, userId);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Delete multiple notifications for user
     */
    public int deleteMultipleNotificationsForUser(List<Long> notificationIds, Long userId) {
        int deletedCount = 0;
        for (Long notificationId : notificationIds) {
            if (deleteNotificationForUser(notificationId, userId)) {
                deletedCount++;
            }
        }
        logger.info("Deleted {} notifications for user {}", deletedCount, userId);
        return deletedCount;
    }
    
    /**
     * Delete all notifications for user
     */
    public void deleteAllNotificationsForUser(Long userId) {
        List<Notification> userNotifications = getNotificationsForUser(userId);
        notificationRepository.deleteAll(userNotifications);
        logger.info("Deleted all {} notifications for user {}", userNotifications.size(), userId);
    }
    
    /**
     * Delete old notifications (cleanup)
     */
    public void deleteOldNotifications(LocalDateTime beforeDate) {
        List<Notification> oldNotifications = notificationRepository.findNotificationsSince(beforeDate);
        notificationRepository.deleteAll(oldNotifications);
        logger.info("Deleted {} old notifications", oldNotifications.size());
    }
    
    /**
     * Delete notifications by alert type for user
     */
    public int deleteNotificationsByAlertTypeForUser(Long userId, Notification.AlertType alertType) {
        List<Notification> notifications = getNotificationsByUserAndAlertType(userId, alertType);
        notificationRepository.deleteAll(notifications);
        logger.info("Deleted {} notifications of type {} for user {}", notifications.size(), alertType, userId);
        return notifications.size();
    }
    
    /**
     * Mark all notifications as read for user
     */
    public void markAllAsReadForUser(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotificationsForUser(userId);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
        logger.info("All {} notifications marked as read for user {}", unreadNotifications.size(), userId);
    }
    
    /**
     * Get notification by ID for user (security check)
     */
    public Optional<Notification> getNotificationForUser(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (notification.getRecipientUser().getUserId().equals(userId)) {
                return notificationOpt;
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get notifications with pagination
     */
    public List<Notification> getNotificationsForUserWithPagination(Long userId, int page, int size) {
        // This would require Pageable implementation in repository
        // For now, return all notifications
        return getNotificationsForUser(userId);
    }
    
    /**
     * Search notifications by content
     */
    public List<Notification> searchNotificationsForUser(Long userId, String searchTerm) {
        try {
            return notificationRepository.findByRecipientUserUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .filter(n -> n.getMessageContent().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
        } catch (Exception e) {
            logger.error("Error searching notifications for user {}: {}", userId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
}
