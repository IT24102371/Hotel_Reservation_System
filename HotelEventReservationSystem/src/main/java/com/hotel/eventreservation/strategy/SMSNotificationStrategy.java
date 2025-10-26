package com.hotel.eventreservation.strategy;

import com.hotel.eventreservation.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("sMSNotificationStrategy")
public class SMSNotificationStrategy implements NotificationStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(SMSNotificationStrategy.class);
    
    @Override
    public boolean sendNotification(Notification notification) {
        try {
            // Mock SMS implementation - in real scenario, integrate with SMS provider like Twilio
            String phoneNumber = notification.getRecipientUser().getPhone();
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                logger.warn("No phone number available for user: {}", notification.getRecipientUser().getUsername());
                return false;
            }
            
            String smsContent = buildSMSContent(notification);
            logger.info("SMS would be sent to {}: {}", phoneNumber, smsContent);
            
            // Simulate SMS sending (without blocking sleep)
            logger.info("SMS notification sent successfully to: {}", phoneNumber);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS notification to: {}", notification.getRecipientUser().getPhone(), e);
            return false;
        }
    }
    
    @Override
    public String getStrategyType() {
        return "SMS";
    }
    
    private String buildSMSContent(Notification notification) {
        StringBuilder content = new StringBuilder();
        content.append("Hotel Event: ");
        content.append(notification.getMessageContent());
        content.append(" [").append(notification.getAlertType().toString()).append("]");
        return content.toString();
    }
}
