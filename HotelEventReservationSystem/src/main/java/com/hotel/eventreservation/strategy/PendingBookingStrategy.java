package com.hotel.eventreservation.strategy;

import com.hotel.eventreservation.model.Booking;
import com.hotel.eventreservation.model.Notification;
import com.hotel.eventreservation.model.User;
import com.hotel.eventreservation.service.NotificationService;
import com.hotel.eventreservation.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("pendingBookingStrategy")
public class PendingBookingStrategy implements BookingStatusStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(PendingBookingStrategy.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean processBooking(Booking booking) {
        try {
            logger.info("Processing pending booking: {}", booking.getReferenceCode());
            
            // For pending bookings, we typically:
            // 1. Send confirmation notification to guest
            // 2. Notify staff about new booking
            // 3. Reserve venue availability
            // 4. Generate QR code
            
            // Notify guest about booking creation
            notificationService.sendNotification(
                booking.getGuest(),
                "Your booking " + booking.getReferenceCode() + " has been created and is pending confirmation. We will review your request and get back to you soon.",
                Notification.AlertType.BOOKING_CONFIRMATION
            );
            
            // Notify managers about new pending booking
            for (User manager : userService.getUsersByRole("GENERAL_MANAGER")) {
                notificationService.sendNotification(
                    manager,
                    "New booking request: " + booking.getReferenceCode() + " from " + booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName(),
                    Notification.AlertType.COORDINATION_ALERT
                );
            }
            
            // Notify coordinators about new pending booking
            for (User coordinator : userService.getUsersByRole("EVENT_COORDINATOR")) {
                notificationService.sendNotification(
                    coordinator,
                    "New booking request: " + booking.getReferenceCode() + " for " + booking.getEventType() + " on " + booking.getEventDate(),
                    Notification.AlertType.COORDINATION_ALERT
                );
            }
            
            logger.info("Pending booking processed successfully: {}", booking.getReferenceCode());
            return true;
            
        } catch (Exception e) {
            logger.error("Error processing pending booking: {}", booking.getReferenceCode(), e);
            return false;
        }
    }
    
    @Override
    public Booking.BookingStatus getHandledStatus() {
        return Booking.BookingStatus.PENDING;
    }
    
    @Override
    public String getStrategyType() {
        return "PENDING_BOOKING";
    }
}
