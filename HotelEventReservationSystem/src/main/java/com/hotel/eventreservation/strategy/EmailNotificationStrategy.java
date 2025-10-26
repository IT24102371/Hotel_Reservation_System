package com.hotel.eventreservation.strategy;

import com.hotel.eventreservation.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("emailNotificationStrategy")
public class EmailNotificationStrategy implements NotificationStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationStrategy.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Override
    public boolean sendNotification(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipientUser().getEmail());
            message.setSubject("Hotel Event Reservation - " + notification.getAlertType().toString());
            message.setText(buildEmailContent(notification));
            message.setFrom("noreply@hotel-event-reservation.com");
            
            mailSender.send(message);
            logger.info("Email notification sent successfully to: {}", notification.getRecipientUser().getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send email notification to: {}", notification.getRecipientUser().getEmail(), e);
            return false;
        }
    }
    
    @Override
    public String getStrategyType() {
        return "EMAIL";
    }
    
    private String buildEmailContent(Notification notification) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(notification.getRecipientUser().getFirstName()).append(",\n\n");
        content.append(notification.getMessageContent()).append("\n\n");
        content.append("Alert Type: ").append(notification.getAlertType().toString()).append("\n");
        content.append("Sent at: ").append(notification.getCreatedAt()).append("\n\n");
        content.append("Best regards,\nHotel Event Reservation System");
        return content.toString();
    }
}
