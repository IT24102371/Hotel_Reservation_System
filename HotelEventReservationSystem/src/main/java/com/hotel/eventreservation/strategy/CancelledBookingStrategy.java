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

@Component("cancelledBookingStrategy")
public class CancelledBookingStrategy implements BookingStatusStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(CancelledBookingStrategy.class);
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Override
    public boolean processBooking(Booking booking) {
        try {
            logger.info("Processing cancelled booking: {}", booking.getReferenceCode());
            
            // For cancelled bookings, we typically:
            // 1. Send cancellation confirmation to guest
            // 2. Release venue availability
            // 3. Notify staff about cancellation
            // 4. Process any refunds if applicable
            // 5. Invalidate QR code
            
            // Notify guest
            notificationService.sendNotification(
                booking.getGuest(),
                "Your booking " + booking.getReferenceCode() + " has been cancelled.",
                Notification.AlertType.BOOKING_CANCELLATION
            );
            
            // Notify manager(s)
            for (User manager : userService.getUsersByRole("GENERAL_MANAGER")) {
                notificationService.sendNotification(
                    manager,
                    "Booking " + booking.getReferenceCode() + " was cancelled by the guest.",
                    Notification.AlertType.BOOKING_CANCELLATION
                );
            }
            
            // Notify coordinator(s)
            for (User coordinator : userService.getUsersByRole("EVENT_COORDINATOR")) {
                notificationService.sendNotification(
                    coordinator,
                    "Booking " + booking.getReferenceCode() + " was cancelled by the guest.",
                    Notification.AlertType.BOOKING_CANCELLATION
                );
            }
            
            logger.info("Cancelled booking processed successfully: {}", booking.getReferenceCode());
            return true;
            
        } catch (Exception e) {
            logger.error("Error processing cancelled booking: {}", booking.getReferenceCode(), e);
            return false;
        }
    }
    
    @Override
    public Booking.BookingStatus getHandledStatus() {
        return Booking.BookingStatus.CANCELLED;
    }
    
    @Override
    public String getStrategyType() {
        return "CANCELLED_BOOKING";
    }
}
