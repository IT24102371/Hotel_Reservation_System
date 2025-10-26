package com.hotel.eventreservation.strategy;

import com.hotel.eventreservation.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("completedBookingStrategy")
public class CompletedBookingStrategy implements BookingStatusStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(CompletedBookingStrategy.class);
    
    @Override
    public boolean processBooking(Booking booking) {
        try {
            logger.info("Processing completed booking: {}", booking.getReferenceCode());
            
            // For completed bookings, we typically:
            // 1. Send thank you email to guest
            // 2. Release venue availability
            // 3. Archive booking data
            // 4. Generate post-event report
            // 5. Request feedback from guest
            
            logger.info("Completed booking processed successfully: {}", booking.getReferenceCode());
            return true;
            
        } catch (Exception e) {
            logger.error("Error processing completed booking: {}", booking.getReferenceCode(), e);
            return false;
        }
    }
    
    @Override
    public Booking.BookingStatus getHandledStatus() {
        return Booking.BookingStatus.COMPLETED;
    }
    
    @Override
    public String getStrategyType() {
        return "COMPLETED_BOOKING";
    }
}
