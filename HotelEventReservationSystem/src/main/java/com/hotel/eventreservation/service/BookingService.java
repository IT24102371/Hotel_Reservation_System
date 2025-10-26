package com.hotel.eventreservation.service;

import com.hotel.eventreservation.model.*;
import com.hotel.eventreservation.repository.BookingRepository;
import com.hotel.eventreservation.repository.VenueAvailabilityRepository;
import com.hotel.eventreservation.repository.VenueRepository;
import com.hotel.eventreservation.strategy.BookingStatusStrategy;
import com.hotel.eventreservation.util.QRCodeGenerator;
import com.hotel.eventreservation.util.ReferenceCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private VenueAvailabilityRepository venueAvailabilityRepository;
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private QRCodeGenerator qrCodeGenerator;
    
    @Autowired
    private ReferenceCodeGenerator referenceCodeGenerator;
    
    @Autowired
    private Map<String, BookingStatusStrategy> bookingStatusStrategies;
    
    /**
     * Create a new booking
     */
    public Booking createBooking(User guest, Venue venue, String eventType, 
                                LocalDate eventDate, LocalTime startTime, LocalTime endTime,
                                Integer guestCount, String specialRequests,
                                DecorPreferences decorPrefs, CateringPreferences cateringPrefs) {
        
        // Check venue availability
        if (!isVenueAvailable(venue.getVenueId(), eventDate, startTime, endTime)) {
            throw new RuntimeException("Venue is not available for the selected time slot");
        }
        
        // Calculate total cost
        BigDecimal totalCost = calculateTotalCost(venue, startTime, endTime);
        
        // Generate reference code
        String referenceCode = referenceCodeGenerator.generateReferenceCode();
        
        // Create booking
        Booking booking = new Booking(guest, venue, eventType, eventDate, startTime, endTime, guestCount, totalCost);
        booking.setReferenceCode(referenceCode);
        booking.setSpecialRequests(specialRequests);
        
        // Generate QR code
        String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(referenceCode);
        if (qrCodeBase64 != null) {
            booking.setQrCodePath("data:image/png;base64," + qrCodeBase64);
        }
        
        // Save booking
        booking = bookingRepository.save(booking);
        
        // Set up preferences
        if (decorPrefs != null) {
            decorPrefs.setBooking(booking);
            booking.setDecorPreferences(decorPrefs);
        }
        
        if (cateringPrefs != null) {
            cateringPrefs.setBooking(booking);
            booking.setCateringPreferences(cateringPrefs);
        }
        
        // Block venue availability
        blockVenueAvailability(venue.getVenueId(), eventDate, startTime, endTime, booking.getBookingId());
        
        // Process booking using strategy pattern
        processBookingStatus(booking);
        
        logger.info("Booking created successfully: {}", booking.getReferenceCode());
        return booking;
    }
    
    /**
     * Update booking status
     */
    public Booking updateBookingStatus(Long bookingId, Booking.BookingStatus newStatus) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        booking.setBookingStatus(newStatus);
        booking = bookingRepository.save(booking);
        
        // Process booking using strategy pattern
        processBookingStatus(booking);
        
        logger.info("Booking status updated to {} for booking: {}", newStatus, booking.getReferenceCode());
        return booking;
    }
    
    /**
     * Cancel booking
     */
    public Booking cancelBooking(Long bookingId) {
        return updateBookingStatus(bookingId, Booking.BookingStatus.CANCELLED);
    }
    
    /**
     * Confirm booking
     */
    public Booking confirmBooking(Long bookingId) {
        return updateBookingStatus(bookingId, Booking.BookingStatus.CONFIRMED);
    }
    
    /**
     * Complete booking
     */
    public Booking completeBooking(Long bookingId) {
        return updateBookingStatus(bookingId, Booking.BookingStatus.COMPLETED);
    }
    
    /**
     * Find booking by reference code
     */
    public Optional<Booking> findByReferenceCode(String referenceCode) {
        return bookingRepository.findByReferenceCode(referenceCode);
    }
    
    /**
     * Find booking by reference code with eagerly loaded related entities
     */
    public Optional<Booking> findByReferenceCodeWithDetails(String referenceCode) {
        return bookingRepository.findByReferenceCodeWithDetails(referenceCode);
    }
    
    /**
     * Get bookings by guest
     */
    public List<Booking> getBookingsByGuest(Long guestId) {
        return bookingRepository.findByGuestUserId(guestId);
    }
    
    /**
     * Get bookings by venue
     */
    public List<Booking> getBookingsByVenue(Long venueId) {
        return bookingRepository.findByVenueVenueId(venueId);
    }
    
    /**
     * Get bookings by status
     */
    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByBookingStatus(status);
    }
    
    /**
     * Get bookings by date range
     */
    public List<Booking> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByEventDateBetween(startDate, endDate);
    }
    
    /**
     * Get bookings by assigned staff member
     */
    public List<Booking> getBookingsByAssignedStaff(Long staffId) {
        return bookingRepository.findByAssignedCoordinatorId(staffId);
    }
    
    /**
     * Get bookings with catering requirements
     */
    public List<Booking> getBookingsWithCatering() {
        return bookingRepository.findByCateringPreferencesIsNotNull();
    }
    
    /**
     * Find booking by ID
     */
    public Optional<Booking> findById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }
    
    /**
     * Update booking
     */
    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
    
    /**
     * Check if venue is available
     */
    public boolean isVenueAvailable(Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<VenueAvailability> conflicts = venueAvailabilityRepository.findConflictingAvailability(venueId, date, startTime, endTime, VenueAvailability.AvailabilityStatus.AVAILABLE);
        return conflicts.isEmpty();
    }
    
    /**
     * Calculate total cost
     */
    public BigDecimal calculateTotalCost(Venue venue, LocalTime startTime, LocalTime endTime) {
        long hours = java.time.Duration.between(startTime, endTime).toHours();
        return venue.getHourlyRate().multiply(BigDecimal.valueOf(hours));
    }
    
    /**
     * Block venue availability
     */
    private void blockVenueAvailability(Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime, Long bookingId) {
        // Find existing availability slot to update
        List<VenueAvailability> existingSlots = venueAvailabilityRepository.findConflictingAvailability(
            venueId, date, startTime, endTime, VenueAvailability.AvailabilityStatus.AVAILABLE);
        
        if (!existingSlots.isEmpty()) {
            // Update existing slot to BOOKED status
            VenueAvailability slot = existingSlots.get(0);
            slot.setStatus(VenueAvailability.AvailabilityStatus.BOOKED);
            slot.setBookingId(bookingId);
            venueAvailabilityRepository.save(slot);
        } else {
            // Create new BOOKED slot if no existing slot found
            Optional<Venue> venueOpt = venueRepository.findById(venueId);
            if (venueOpt.isPresent()) {
                VenueAvailability availability = new VenueAvailability(
                    venueOpt.get(), date, startTime, endTime, VenueAvailability.AvailabilityStatus.BOOKED);
                availability.setBookingId(bookingId);
                venueAvailabilityRepository.save(availability);
            }
        }
    }
    
    /**
     * Process booking using strategy pattern
     */
    private void processBookingStatus(Booking booking) {
        try {
            if (booking == null || booking.getBookingStatus() == null) {
                logger.warn("Booking or booking status is null, skipping strategy processing");
                return;
            }
            
            String strategyKey = booking.getBookingStatus().toString().toLowerCase() + "BookingStrategy";
            BookingStatusStrategy strategy = bookingStatusStrategies.get(strategyKey);
            
            if (strategy != null) {
                strategy.processBooking(booking);
            } else {
                logger.warn("No strategy found for booking status: {}", booking.getBookingStatus());
            }
        } catch (Exception e) {
            logger.error("Error processing booking status for booking: {}", 
                        booking != null ? booking.getReferenceCode() : "null", e);
        }
    }
}
