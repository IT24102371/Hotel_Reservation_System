package com.hotel.eventreservation.service;

import com.hotel.eventreservation.model.Notification;
import com.hotel.eventreservation.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationCleanupService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Value("${app.notification.cleanup.days:30}")
    private int cleanupDays;
    
    @Value("${app.notification.cleanup.enabled:true}")
    private boolean cleanupEnabled;
    
    /**
     * Scheduled cleanup of old notifications
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldNotifications() {
        if (!cleanupEnabled) {
            logger.debug("Notification cleanup is disabled");
            return;
        }
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cleanupDays);
            List<Notification> oldNotifications = notificationRepository.findNotificationsSince(cutoffDate);
            
            if (!oldNotifications.isEmpty()) {
                notificationRepository.deleteAll(oldNotifications);
                logger.info("Cleaned up {} old notifications (older than {} days)", 
                           oldNotifications.size(), cleanupDays);
            } else {
                logger.debug("No old notifications found for cleanup");
            }
        } catch (Exception e) {
            logger.error("Error during notification cleanup", e);
        }
    }
    
    /**
     * Manual cleanup of old notifications
     */
    public int cleanupOldNotificationsManually() {
        if (!cleanupEnabled) {
            logger.warn("Notification cleanup is disabled");
            return 0;
        }
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cleanupDays);
            List<Notification> oldNotifications = notificationRepository.findNotificationsSince(cutoffDate);
            
            if (!oldNotifications.isEmpty()) {
                notificationRepository.deleteAll(oldNotifications);
                logger.info("Manually cleaned up {} old notifications (older than {} days)", 
                           oldNotifications.size(), cleanupDays);
                return oldNotifications.size();
            } else {
                logger.info("No old notifications found for manual cleanup");
                return 0;
            }
        } catch (Exception e) {
            logger.error("Error during manual notification cleanup", e);
            throw e;
        }
    }
    
    /**
     * Cleanup notifications by alert type
     */
    public int cleanupNotificationsByAlertType(Notification.AlertType alertType) {
        try {
            List<Notification> notifications = notificationRepository.findByAlertType(alertType);
            if (!notifications.isEmpty()) {
                notificationRepository.deleteAll(notifications);
                logger.info("Cleaned up {} notifications of type {}", 
                           notifications.size(), alertType);
                return notifications.size();
            }
            return 0;
        } catch (Exception e) {
            logger.error("Error cleaning up notifications by alert type: {}", alertType, e);
            throw e;
        }
    }
    
    /**
     * Get cleanup statistics
     */
    public CleanupStats getCleanupStats() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cleanupDays);
        List<Notification> oldNotifications = notificationRepository.findNotificationsSince(cutoffDate);
        
        return new CleanupStats(
            oldNotifications.size(),
            cleanupDays,
            cleanupEnabled
        );
    }
    
    public static class CleanupStats {
        private final int oldNotificationsCount;
        private final int cleanupDays;
        private final boolean cleanupEnabled;
        
        public CleanupStats(int oldNotificationsCount, int cleanupDays, boolean cleanupEnabled) {
            this.oldNotificationsCount = oldNotificationsCount;
            this.cleanupDays = cleanupDays;
            this.cleanupEnabled = cleanupEnabled;
        }
        
        public int getOldNotificationsCount() {
            return oldNotificationsCount;
        }
        
        public int getCleanupDays() {
            return cleanupDays;
        }
        
        public boolean isCleanupEnabled() {
            return cleanupEnabled;
        }
    }
}



