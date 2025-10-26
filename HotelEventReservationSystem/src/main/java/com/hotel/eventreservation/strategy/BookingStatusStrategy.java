package com.hotel.eventreservation.strategy;

import com.hotel.eventreservation.model.Booking;

public interface BookingStatusStrategy {
    
    /**
     * Process the booking based on its status
     * @param booking The booking to process
     * @return true if processing was successful, false otherwise
     */
    boolean processBooking(Booking booking);
    
    /**
     * Get the booking status this strategy handles
     * @return The booking status
     */
    Booking.BookingStatus getHandledStatus();
    
    /**
     * Get the strategy type name
     * @return The strategy type identifier
     */
    String getStrategyType();
}
