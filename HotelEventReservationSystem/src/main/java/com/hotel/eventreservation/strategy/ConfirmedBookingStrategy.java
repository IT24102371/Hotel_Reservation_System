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

@Component("confirmedBookingStrategy")
public class ConfirmedBookingStrategy implements BookingStatusStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfirmedBookingStrategy.class);
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Override
    public boolean processBooking(Booking booking) {
        try {
            logger.info("Processing confirmed booking: {}", booking.getReferenceCode());
            
            // For confirmed bookings, we typically:
            // 1. Send confirmation details to guest
            // 2. Notify event coordinator and catering team
            // 3. Block venue availability
            // 4. Set up event reminders
            
            // Notify guest
            notificationService.sendNotification(
                booking.getGuest(),
                "Your booking " + booking.getReferenceCode() + " is confirmed.",
                Notification.AlertType.BOOKING_CONFIRMATION
            );
            
            // Notify coordinator(s)
            for (User coordinator : userService.getUsersByRole("EVENT_COORDINATOR")) {
                notificationService.sendNotification(
                    coordinator,
                    "A booking has been confirmed: " + booking.getReferenceCode(),
                    Notification.AlertType.COORDINATION_ALERT
                );
            }
            
            // Notify catering leader(s)
            for (User cateringLead : userService.getUsersByRole("CATERING_TEAM_LEADER")) {
                notificationService.sendNotification(
                    cateringLead,
                    "Catering required for booking: " + booking.getReferenceCode(),
                    Notification.AlertType.CATERING_CONFIRMED
                );
            }
            
            logger.info("Confirmed booking processed successfully: {}", booking.getReferenceCode());
            return true;
            
        } catch (Exception e) {
            logger.error("Error processing confirmed booking: {}", booking.getReferenceCode(), e);
            return false;
        }
    }
    
    @Override
    public Booking.BookingStatus getHandledStatus() {
        return Booking.BookingStatus.CONFIRMED;
    }
    
    @Override
    public String getStrategyType() {
        return "CONFIRMED_BOOKING";
    }
}
